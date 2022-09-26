package com.arka.streamingserv.utils;

import com.arka.helperlib.constants.enums.ErrorNameEnum;
import com.arka.helperlib.constants.vo.ErrorVO;
import com.arka.helperlib.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

public class ErrorUtils {

    private static String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    private static String NOT_FOUND_ERROR_MESSAGE = "Not Found";

    private static String UN_AUTHORIZED_MESSAGE = "Unauthorized";

    private static String BAD_REQUEST = "Bad Request";

    public static ErrorVO formUnauthorizedErrorVO(JsonNode errorJson) {
        String unAuthorizedMessage = UN_AUTHORIZED_MESSAGE;
        if(JsonUtils.isValidField(errorJson, JsonUtils.ERROR)) {
            unAuthorizedMessage = errorJson.get(JsonUtils.ERROR).asText();
        }
        ErrorVO errorVO = new ErrorVO();
        errorVO.setName(ErrorNameEnum.UNAUTHORIZED);
        errorVO.setMessage(unAuthorizedMessage);
        errorVO.setDetails(new ArrayList<>());
        return errorVO;
    }

    public static ErrorVO formInternalServerErrorVO(JsonNode errorJson) {
        String internalServerErrorMessage = INTERNAL_SERVER_ERROR_MESSAGE;
        if(JsonUtils.isValidField(errorJson, JsonUtils.ERROR)) {
            internalServerErrorMessage = errorJson.get(JsonUtils.ERROR).asText();
        }
        ErrorVO errorVO = new ErrorVO();
        errorVO.setName(ErrorNameEnum.INTERNAL_SERVICE_ERROR);
        errorVO.setMessage(internalServerErrorMessage);
        errorVO.setDetails(new ArrayList<>());
        return errorVO;
    }

    public static ErrorVO formNotFoundErrorVO(JsonNode errorJson) {
        String notFoundErrorMessage = NOT_FOUND_ERROR_MESSAGE;
        if(JsonUtils.isValidField(errorJson, JsonUtils.ERROR)) {
            notFoundErrorMessage = errorJson.get(JsonUtils.ERROR).asText();
        }
        ErrorVO errorVO = new ErrorVO();
        errorVO.setName(ErrorNameEnum.NOT_FOUND);
        errorVO.setMessage(notFoundErrorMessage);
        errorVO.setDetails(new ArrayList<>());
        return errorVO;
    }


    public static ErrorVO formBadRequestErrorVO(JsonNode errorJson) {
        String badRequest = BAD_REQUEST;
        if(JsonUtils.isValidField(errorJson, JsonUtils.ERROR)) {
            badRequest = errorJson.get(JsonUtils.ERROR).asText();
        }
        ErrorVO errorVO = new ErrorVO();
        errorVO.setName(ErrorNameEnum.BAD_REQUEST);
        errorVO.setMessage(badRequest);
        errorVO.setDetails(new ArrayList<>());
        return errorVO;
    }
}

