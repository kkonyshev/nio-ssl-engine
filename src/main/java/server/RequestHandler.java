package server;

public interface RequestHandler<RequestDto, ResponseDto> {
    ResponseDto handle(RequestDto request);
}
