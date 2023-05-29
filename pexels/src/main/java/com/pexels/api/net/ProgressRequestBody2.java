package com.pexels.api.net;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody2 extends RequestBody {
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;
    private String content_type;
    private boolean mIsCancelled;

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final long PROGRESS_UPDATE_DELAY_MILLIS = 150L;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);

        void onError(String message);

        void onFinish();
    }

    public ProgressRequestBody2(final File file, String content_type, final UploadCallbacks listener) {
        this.content_type = content_type;
        mFile = file;
        mListener = listener;
        mIsCancelled = false;
    }

    public void cancel() {
        mIsCancelled = true;
    }

    public boolean isCancelled() {
        return mIsCancelled;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type + "/*");
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (FileInputStream in = new FileInputStream(mFile)) {
            long uploaded = 0;
            int read;
            Handler handler = new Handler(Looper.getMainLooper());

            long lastUpdated = 0;
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                if (System.currentTimeMillis() > lastUpdated + PROGRESS_UPDATE_DELAY_MILLIS) {
                    handler.post(new ProgressUpdater(uploaded, fileLength));
                    lastUpdated = System.currentTimeMillis();
                }
                uploaded += read;
                sink.write(buffer, 0, read);

                if (isCancelled()) {
                    mListener.onError("Cancelled");
                    sink.flush();
                    return;
                }

            }
//            sink.flush();

            if (!isCancelled()) mListener.onFinish();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int) (100 * mUploaded / mTotal));
        }
    }
}
