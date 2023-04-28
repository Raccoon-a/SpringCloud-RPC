package model;

public class PersonInfo {

    private Integer infoId;

    private String infoBody;

    private PersonInfo(Builder builder) {
        setInfoId(builder.infoId);
        setInfoBody(builder.infoBody);
    }

    public PersonInfo(){

    }
    public Integer getInfoId() {
        return infoId;
    }

    public void setInfoId(Integer infoId) {
        this.infoId = infoId;
    }

    public String getInfoBody() {
        return infoBody;
    }

    public void setInfoBody(String infoBody) {
        this.infoBody = infoBody;
    }


    public static final class Builder {
        private Integer infoId;
        private String infoBody;

        public Builder() {
        }

        public Builder infoId(Integer val) {
            infoId = val;
            return this;
        }

        public Builder infoBody(String val) {
            infoBody = val;
            return this;
        }

        public PersonInfo build() {
            return new PersonInfo(this);
        }
    }
}
