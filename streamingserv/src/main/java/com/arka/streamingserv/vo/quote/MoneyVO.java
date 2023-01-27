package com.arka.streamingserv.vo.quote;

import com.arka.helperlib.constants.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyVO {

    @Min(value = 1, message = "invalid.income")
    private Double value;

    private CurrencyCode currency = CurrencyCode.INR;

}
