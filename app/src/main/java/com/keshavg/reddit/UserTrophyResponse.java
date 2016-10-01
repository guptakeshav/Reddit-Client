package com.keshavg.reddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;

/**
 * Created by keshavgupta on 9/30/16.
 */

public class UserTrophyResponse {
    public class Trophy {
        public class Data {

            @Getter
            private String name;

            @Getter
            private String description;

            @Getter
            @SerializedName("icon_70") private String icon;
        }

        @Getter
        private Data data;
    }

    private class Response {
        @Getter
        @SerializedName("trophies") private List<Trophy> trophyList;
    }

    private Response data;

    public List<Trophy> getTrophyList() {
        return data.getTrophyList();
    }
}
