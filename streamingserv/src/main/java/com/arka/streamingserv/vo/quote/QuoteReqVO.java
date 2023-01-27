package com.arka.streamingserv.vo.quote;

import com.arka.streamingserv.constants.ProductCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class QuoteReqVO {

    @NotBlank(message = "enquiry.id.required")
    private String enquiryId;

    private int tenure;

    private ProductCode productCode;

}
