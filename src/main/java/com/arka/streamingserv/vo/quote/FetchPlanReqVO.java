package com.arka.streamingserv.vo.quote;

import com.arka.streamingserv.constants.ProductCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FetchPlanReqVO {

    private String enquiryId;

    private int tenure;

    private String productId;

}
