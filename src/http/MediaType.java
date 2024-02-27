package http;

public enum MediaType {
    JSON () {
        @Override
        public String getContentType() {
            return "application/json";
        }

        @Override
        public String value() {
            return "json";
        }
    },
    HTML () {

        @Override
        public String getContentType() {
            return "text/html";
        }

        @Override
        public String value() {
            return "html";
        }
    };

    public abstract String getContentType();
    public abstract String value();
}
