package cn.yuyake.common.error;

import org.slf4j.helpers.MessageFormatter;

public class GameErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private IServerError error;

    private GameErrorException(IServerError error, String message, Throwable exp) {
        super(message, exp);
        this.error = error;

    }

    private GameErrorException(IServerError error, String message) {
        super(message);
        this.error = error;
    }

    public IServerError getError() {
        return error;
    }

    public static Builder newBuilder(IServerError error) {
        return new Builder(error);
    }

    public static class Builder {
        private IServerError error;
        private String message;
        private Throwable exp;

        public Builder(IServerError error) {
            this.error = error;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder message(String format, Object... args) {
            this.message = MessageFormatter.arrayFormat(format, args).getMessage();
            return this;
        }

        public Builder causeBy(Throwable exp) {
            this.exp = exp;
            return this;
        }

        public GameErrorException build() {
            String msg = this.error.toString();
            StringBuilder str = new StringBuilder(msg);

            if (this.message != null) {
                str.append("   ").append(this.message);
            }
            if (this.exp == null) {
                return new GameErrorException(this.error, str.toString());
            } else {
                return new GameErrorException(this.error, str.toString(), this.exp);
            }

        }

    }

    public static void main(String[] args) {
        String msg = MessageFormatter.arrayFormat("你好,{}", new String[] {"cccc"}).getMessage();
        System.out.println(msg);
    }
}
