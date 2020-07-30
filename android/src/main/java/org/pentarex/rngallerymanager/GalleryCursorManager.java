package org.pentarex.rngallerymanager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by pentarex on 26.01.18.
 */

public class GalleryCursorManager {

    public static Cursor getAssetCursor(String requestedType, String albumName, ReactApplicationContext reactContext) {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.TITLE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };


        String selection;
        switch (requestedType.toLowerCase()){
            case "image": {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                break;
            }
            case "video": {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                break;
            }
            default: {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                break;
            }
        }

        String[] selectionArgs = null;
        if (albumName != null) {
            selection += ") AND (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "= ?";
            selectionArgs = new String[] { albumName };
        }

        String sortByAndLimit = MediaStore.Files.FileColumns.DATE_ADDED + " DESC ";

        ContentResolver contentResolver = reactContext.getContentResolver();
        Uri queryUri = MediaStore.Files.getContentUri("external");

        return contentResolver.query(queryUri, projection, selection, selectionArgs, sortByAndLimit);
    }

    public static Collection<AlbumFolder> getAlbumCursor(ReactApplicationContext reactContext, String mediaType) {
        String[] projection = new String[] {
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.DATE_MODIFIED,
                MediaStore.Video.VideoColumns.DATA,
        };


        ContentResolver contentResolver = reactContext.getContentResolver();
        Uri queryUri = MediaStore.Files.getContentUri("external");
        String BUCKET_GROUP_BY = "";

        switch (mediaType) {
          case "image": {
            BUCKET_GROUP_BY = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
            break;
          }
          case "video": {
            BUCKET_GROUP_BY = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            break;
          }
          default: {
            BUCKET_GROUP_BY = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                              + " OR "
                              + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
          }
        }

        String sortBy = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " COLLATE NOCASE ASC, " + MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Cursor allMediaItems = contentResolver.query(queryUri, projection, BUCKET_GROUP_BY, null, sortBy);

        HashMap<String, AlbumFolder> folders = new HashMap<String, AlbumFolder>();

        while (allMediaItems != null && allMediaItems.moveToNext()) {
            String     path      = allMediaItems.getString(allMediaItems.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
            String     title     = allMediaItems.getString(allMediaItems.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));

            AlbumFolder albumFolder = folders.containsKey(title) ? folders.get(title) : new AlbumFolder(title, path);
            albumFolder.incItemsCount();
            folders.put(title, albumFolder);
        }

        if (allMediaItems != null) {
            allMediaItems.close();
        }

        return folders.values();
    }

    public static class AlbumFolder {
        String title;
        String firstImagePath;
        int itemsCount;

        AlbumFolder(String title, String firstImagePath) {
            this.title = title;
            this.firstImagePath = firstImagePath;
            itemsCount = 0;
        }

        int incItemsCount() {
            return this.itemsCount += 1;
        }
    }
}
