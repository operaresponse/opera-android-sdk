package com.shipit.motorgator;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sumeet on 9/18/14.
 */
public class BingResultContainer {

    private static class D {
        @SerializedName("results")
        private List<BingImage> _results;

        public List<BingImage> results() {
            return _results;
        }
    }

    public static class BingImage {
        @SerializedName("Title")
        private String _title;

        @SerializedName("MediaUrl")
        private String _url;

        @SerializedName("Width")
        private int _width;

        @SerializedName("Height")
        private int _height;

        public String title() {
            return _title;
        }

        public String url() {
            return _url;
        }

        public int width() {
            return _width;
        }

        public int height() {
            return _height;
        }
    }

    @SerializedName("d")
    private D _d;

    public List<BingImage> images() {
        return _d != null ? _d.results() : null;
    }
}
