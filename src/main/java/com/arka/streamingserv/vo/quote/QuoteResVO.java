package com.arka.streamingserv.vo.quote;


import lombok.Data;

@Data
public class QuoteResVO {

//    private int tenure;

    private String productCode = "Sakthi skv";

    private InsurerVO insurer;

    private String quoteKey;

    private MoneyVO sumInsured;

    private PremiumVO premiumWithoutTax;

}
