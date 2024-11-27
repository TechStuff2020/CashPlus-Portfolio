### 30 second summary
Going beyond off-the-shelf portfolio trackers and academic metrics such as IRR, XIRR, TWR and MWRs, this unique Portfolio tracker helps you answer a simple question: Did my stock trading beat the index? To get the right answer, we use a concept of **Cash-aware portfolio** which is described below. The idle cash in a portfolio, even when earning small **interest**, has an **opportunity cost**. Brokerage **fees, capital gains** tax also cut into real returns. Dividends could stay in cash (and earn interest) or be reinvested. Only after all of that is factored in, we can correctly compare the outcome of our trading behavior, against a plain BUY and HOLD index strategy. Based on user's geography, base curreency and a benchmark index can be chosen, e.g. Dow Jones, Eurostoxx 50, S&P 500, DAX, SMI, or even the 60:40 index or any ETF. 

### Introduction and genesis of idea
According to media, year 2022 saw a record number of first-time investors! A large portion of Gen-Z population is beginning to earn, have investible savings. Besides this data - which may be real or cooked up ![Image](https://user-images.githubusercontent.com/20066864/243864065-292f45a0-8d9f-4091-963b-ec8aee2791c9.png) - one can feel that, discussions in many social circles are beginning to involve "stock tips" or innocent brags about gains from Apple/Netflix/Tesla, etc etc. Over time, one wonders:

### Is DIY investing worth it?

Again, data shows that even professional fund managers do worse than the index - let alone new beginners. Yet, pride, over confidence or plain beginner's luck, makes many people invest on their own. In other words, a majority beginners - even with substantial investment amounts - opt for **self-service or advisory** mode, and prefer to make investment decisions themselves. Therefore, just like a routine doctor's check-up for bodily health, an investment portfolio too needs a periodic health check-up.

### My broker / bank sends me all the reports anyway. Why this?

... because
1. Their reports are wrong! This may be a hard pill to swallow, but TWR (time weighted return) is deemed to be the industry standard for portfolio tracking. This method skips the timing of inflows and ouflows, which is a crucial factor in performance. Duh!  ![Image](https://user-images.githubusercontent.com/20066864/243864329-9cc0cc55-bd70-4fc0-bd2d-0f714a5a063f.png)
2. Even if the report shows MWR (money weighted returns) or IRR (internal rate of return), the evaluation is still logically wrong because - changes to cash as well as returns from cash are not accounted for. In other words, the portfolio is not "Cash aware". so, a realistic **comparision against benchmark index** is either missing or incorrectly calculated. When money market rates are hovering around 3-4% in US/EU in early 2023, this difference can be substantial.
3. They 'forget' about transaction fees or capital gains tax. A $100,000 portfolio of stocks isn't actually worth $100,000 due to fees and taxes.
4. Their incentives are against you. Brokers earn commissions with each trade. So, they will **NEVER** suggest you to quit trading and invest in a boring, low fee index fund/ETF. In fact, they'd entice clients with a new shiny stock tip.

For above reasons (and many more), this project is launched in June 2023.

> Please note that this is not a classic portfolio tracker. There are far too many portals and software applications doing the same already. [GetQuin](getquin.com), [Sharesight](https://www.sharesight.com), [Seeking Alpha](https://seekingalpha.com), to name a few, in addition to your broker's reports!

The difference between an "off the shelf" portfolio tracker and this tool is described below. 

![Image](https://user-images.githubusercontent.com/20066864/243858729-5bbe9e64-e845-442c-8245-cb283704abda.png)

- Red line: classic portfolio value tracker. Note that Buy and Sell trades cause the portfolio value to change. 
- Blue line: this is "**Cash-aware**" portfolio tracking. Each BUY trade simulates a withdrawal from Cash. Corresponding Cash amount would stop earning interest. Also, as this cash was available at hand (it is unlikely that the investor really needed it to pay rent or food), it is factored into the initial investment size of portfolio. Similarly, each SELL trade would increase Cash, as opposed to portfolio's value suddenly dropping as seen in red line. This cash would also earn interest as per the reference rate of money market fund. 

> Thus, a 'tweaked' calculation method offers a better, more real-life way to assess portfolio performance. It also makes benchmarking against index more realistic. As equities should be invested with a time horizon of at least 3-5 years, practically it is fair to assume that cash dips and gains from BUY/SELL trades are done from a separate Cash account earmarked for investing, rather than an expense account. The Cash is treated as a part of investment, as it counts towards the opportunity cost of holding an index fund. More on this - https://github.com/picaYmica/Portfolio-Doctor/wiki/Introduction-to-'Cash-aware'-portfolio 

With benchmarking correctly done, the tool can now answer many questions and run several what-if scenarios that offer useful insights to the investor. Particularly, if users don't wish to use lumpsum investment to avoid **market timing**, there is also a feature to simulate staggered regular purchases. Users can then compare their actual performance with such What-if scenarios.

![Image](https://user-images.githubusercontent.com/20066864/243866423-378681d8-fa5b-4a51-8afd-931c68faca28.png)

![image](https://github.com/picaYmica/Portfolio-Doctor/assets/20066864/4a9c993f-2d32-4691-a3bb-404d3311db0c)

### What else does the portfolio analyzer do?

Besides performance vs a benchmark, the tool can also assess the volatality of your portfolio against a reference index. It can also tell you how well the portfolio is diversified, by grouping your equities in sectors, regions and type of stocks you hold, e.g. momentum, growth, value, etc.

![image](https://github.com/TechStuff2020/CashPlus-Portfolio/assets/20066864/86be045f-a60b-407f-abe2-f72c150ea922)

### Great! Where is the technical ReadMe.txt or installation instructions?

Please refer /src directory. This ReadMe is meant to be a one-pager overview of the project. The project is a REST API, with WordPress content management front-end without any need for any user data storage. User's trading data is ephemeral, so that data privacy concerns do not arise. Only the stock ticker symbols are sent to [Alpha-Vantage](https://www.alphavantage.co/#about) to get the market prices. All calculations happen local device's memory, where the API is installed.

Setting up a portfolo is quite intuitive, as shown below: 

![image](https://github.com/TechStuff2020/Portfolio-Doctor/assets/20066864/2da5dc15-5f68-4c6d-a6b9-fb856d2ed551)

Supported benchmark indices and currencies:

![image](https://github.com/TechStuff2020/Portfolio-Doctor/assets/20066864/70482d6a-cb58-4d72-9ad2-4782b3c7d28f) ![image](https://github.com/TechStuff2020/Portfolio-Doctor/assets/20066864/6ee5e464-9ba8-4e9b-a437-2114a48e3fc3)

### I like this! How can I support this initiative? 

Firstly, thank you! If you're a techie, feel free to pull and contribute to the repo. It is open source. If you wish to use/customize the software, send an email to hello-(at)-portfoliodoc.app  

