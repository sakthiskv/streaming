package com.arka.streamingserv.vo.quote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumVO {

    private MoneyVO basePremium;

    private MoneyVO premiumWithoutDiscount;

    private Map<String, MoneyVO> addons;

    private MoneyVO totalPremium;

    private double taxPercentage = 18;

}
