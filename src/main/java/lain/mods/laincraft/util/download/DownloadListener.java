package lain.mods.laincraft.util.download;

public abstract interface DownloadListener
{

    public abstract void onDownloadJobFinished(DownloadJob job);

    public abstract void onDownloadJobProgressChanged(DownloadJob job);

}
