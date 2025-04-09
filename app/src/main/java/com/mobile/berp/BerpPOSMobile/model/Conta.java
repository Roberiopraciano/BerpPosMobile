package com.mobile.berp.BerpPOSMobile.model;

import com.google.gson.annotations.SerializedName;


    public class Conta {
        @SerializedName("type")
        private String type;

        @SerializedName("id")
        private int id;

        @SerializedName("fields")
        private ContaFields fields;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ContaFields getFields() {
            return fields;
        }

        public void setFields(ContaFields fields) {
            this.fields = fields;
        }
    }