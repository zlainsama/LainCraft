package lain.mods.laincraft.util.download;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadJob
{

    private static final int MAX_ATTEMPTS_PER_FILE = 5;
    private final Queue<Downloadable> remainingFiles = new ConcurrentLinkedQueue();
    private final List<Downloadable> allFiles = Collections.synchronizedList(new ArrayList());
    private final List<Downloadable> failures = Collections.synchronizedList(new ArrayList());
    private final List<Downloadable> successful = Collections.synchronizedList(new ArrayList());
    private final DownloadListener listener;
    private final String name;
    private final boolean ignoreFailures;
    private final AtomicInteger remainingThreads = new AtomicInteger();
    private boolean started = false;

    public DownloadJob(String name, boolean ignoreFailures, DownloadListener listener)
    {
        this(name, ignoreFailures, listener, null);
    }

    public DownloadJob(String name, boolean ignoreFailures, DownloadListener listener, Collection<Downloadable> files)
    {
        this.name = name;
        this.ignoreFailures = ignoreFailures;
        this.listener = listener;
        if (files != null)
            addDownloadables(files);
    }

    public void addDownloadables(Collection<Downloadable> downloadables)
    {
        if (started)
            throw new IllegalStateException("Cannot add to download job that has already started");

        allFiles.addAll(downloadables);
        remainingFiles.addAll(downloadables);
    }

    public void addDownloadables(Downloadable[] downloadables)
    {
        if (started)
            throw new IllegalStateException("Cannot add to download job that has already started");

        for (Downloadable downloadable : downloadables)
        {
            allFiles.add(downloadable);
            remainingFiles.add(downloadable);
        }
    }

    public int getFailures()
    {
        return failures.size();
    }

    public String getName()
    {
        return name;
    }

    public float getProgress()
    {
        float max = allFiles.size();
        if (max == 0.0F)
            return 1.0F;
        float done = successful.size();
        return done / max;
    }

    public int getSuccessful()
    {
        return successful.size();
    }

    public boolean isComplete()
    {
        return (started) && (remainingFiles.isEmpty()) && (remainingThreads.get() == 0);
    }

    public boolean isStarted()
    {
        return started;
    }

    private void popAndDownload()
    {
        Downloadable downloadable;
        while ((downloadable = (Downloadable) remainingFiles.poll()) != null)
        {
            if (downloadable.getNumAttempts() > 5)
            {
                if (!ignoreFailures)
                    failures.add(downloadable);
                System.out.println("Gave up trying to download " + downloadable.getUrl() + " for job '" + name + "'");
            }
            else
            {
                try
                {
                    String result = downloadable.download();
                    successful.add(downloadable);
                    System.out.println("Finished downloading " + downloadable.getTarget() + " for job '" + name + "'" + ": " + result);
                }
                catch (Throwable t)
                {
                    System.out.println("Couldn't download " + downloadable.getUrl() + " for job '" + name + "'");
                    System.out.println(t);
                    remainingFiles.add(downloadable);
                }

                listener.onDownloadJobProgressChanged(this);
            }
        }
        if (remainingThreads.decrementAndGet() <= 0)
            listener.onDownloadJobFinished(this);
    }

    public boolean shouldIgnoreFailures()
    {
        return ignoreFailures;
    }

    public void startDownloading(ThreadPoolExecutor executorService)
    {
        if (started)
            throw new IllegalStateException("Cannot start download job that has already started");
        started = true;

        if (allFiles.isEmpty())
        {
            System.out.println("Download job '" + name + "' skipped as there are no files to download");
            listener.onDownloadJobFinished(this);
        }
        else
        {
            int threads = executorService.getMaximumPoolSize();
            remainingThreads.set(threads);
            System.out.println("Download job '" + name + "' started (" + threads + " threads, " + allFiles.size() + " files)");
            for (int i = 0; i < threads; i++)
                executorService.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        popAndDownload();
                    }
                });
        }
    }

}
