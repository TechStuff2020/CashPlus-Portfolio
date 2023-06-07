# Portfolio Doctor

According to several sources, year 2022 saw a record number of first-time investors! A good portion of Gen-Z is beginning to earn, have investible savings. Besides this data - which may be real or cooked up ![Image](https://user-images.githubusercontent.com/20066864/243864065-292f45a0-8d9f-4091-963b-ec8aee2791c9.png) - one can feel that discussions in many social circles are beginning to involve "stock tips" or innocent brags about gains from Apple/Netflix/Tesla, etc etc. Over time, one wonders:

### Is DIY investing worth it? 

Again, data shows that even professional fund managers do worse than the index - let alone new beginners. Yet, pride, over confidence or plain beginner's luck, makes many people invest on their own. In financial jargon, a majority beginners - regardless of their investment amount - opt for **self-service or advisory** mode, and prefer to make investment decisions themselves. Therefore, just like a routine doctor's check-up for bodily health, an investment portfolio too needs a periodic health check-up.

### My broker / bank sends me all the portfolio performance reports anyway! Why this?

... because
1. Their reports are wrong! This may be a hard pill to swallow, but TWR (time weighted return) is deemed to be the industry standard for portfolio performance. This method skips timing of investments, which is a crucial factor in performance. Duh!  ![Image](https://user-images.githubusercontent.com/20066864/243864329-9cc0cc55-bd70-4fc0-bd2d-0f714a5a063f.png)
2. Even if the report shows MWR (money weighted returns) or IRR (internal rate of return), the evaluation is still logically wrong because - changes to cash as well as returns from Cash are not accounted for. In other words, the portfolio is not "Cash aware". so, a realistic **comparision against benchmark index** is either missing or incorrectly calculated. 
3. Their incentives are against you. Brokers earn commissions with each trade. So, they will **NEVER** suggest you to quit trading and invest in a boring, low fee index fund/ETF. In fact, enticing clients with a new shiny stock works well for them.

For above reasons (and many more), this project is launched. 

> Please note that this is not a classic portfolio tracker. There are far too many portals and applications existing already. [GetQuin](getquin.com), [Sharesight](https://www.sharesight.com), [Seeking Alpha](https://seekingalpha.com), to name a few, in addition to your broker's reports!  

The difference between an "off the shelf" portfolio tracker and **"Portfolio Doctor"** is described below. 

![Image](https://user-images.githubusercontent.com/20066864/243858729-5bbe9e64-e845-442c-8245-cb283704abda.png)

- Red line: classic portfolio value tracker. Note that Buy and Sell trades cause the portfolio value to change. 
- Blue line: this is "**Cash-aware**" portfolio tracking. Each BUY trade simulates a withdrawal from Cash. Corresponding Cash amount would stop earning interest, and would be invested. Also, as this cash was available at hand (it is unlikely that the investor really needed it to pay rent or food), it is factored into the initial investment size of portfolio. Similarly, each SELL trade would cause money to flow into Cash, as opposed to portfolio's value dropping as seen in red line. This amount would also earn interest according to a reference rate of money market liquid fund. 

> Thus, a 'tweaked' calculation method offers a better, more real-life way to assess portfolio performance. It also makes benchmarking against index more accurate and realistic. As equities should be invested with a time horizon of at least 3-5 years, practically it is fair to assume that Cash dips and rises from BUY/SELL trades are in/out of a separate Cash account earmarked for investing, rather than a regular cash account. This Cash account is treated as a part of investment. It counts towards the opportunity cost of using an index fund investment strategy. 

With benchmarking correctly done, the "Doctor" can now answer many questions and run several what-if scenarios that offer useful insights to the investor.

![Image](https://user-images.githubusercontent.com/20066864/243866423-378681d8-fa5b-4a51-8afd-931c68faca28.png)

### Great! Where is the technical ReadMe.txt or installation instructions?

Please refer /src directory. This ReadMe is meant to be a one-pager overview of the project.
