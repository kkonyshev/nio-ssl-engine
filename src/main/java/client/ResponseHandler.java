package client;

public interface ResponseHandler<ResponseDto> {
    void handle(ResponseDto responseDto);
}
