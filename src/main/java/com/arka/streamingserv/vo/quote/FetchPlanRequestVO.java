package com.arka.streamingserv.vo.quote;

import com.arka.streamingserv.constants.ProductCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FetchPlanRequestVO {

    private String enquiryId;

    private int tenure;

    private String productId;

    private String categoryId;

    private String productCode;

}
