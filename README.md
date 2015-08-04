# Web crawling, XPath, XSLT
> Full name:  Yunchen Wei

> SEAS login: yunchenw

**Which features did you implement?**
  - MileStone2: entire requirement
  - Crawler and Servlet should not be started as two process at the same time.
    1. Start the server, run the crawler (Basically, add user and channels before the crawler's running)
    2. Start the crawler from the CrawlerServlet (URL: /crawler)

**Did you complete any extra-credit tasks? If so, which ones?**
  - DFA-based XPath engine
  - Channel subscriptions
  - Crawler web interface

**Any special instructions for building and running your solution?**
  - Import servlet.jar into Eclipse and run it as dynamic web project.
  - Crawler interface needs admin right. 
    - Create a user with username admin. (This is necessary to visit the /crawler page)
  - check web.xml for the exact path matching for servlets.
    - Ex:
    - Signin page: localhost:8080/[project_name]/signin
    - Crawler-Interface page: localhost:8080/[project_name]/crawler
  - By using the web-interface of Crawler, crawler and the server is running in the same process.
  - Crawler state is retrieving from database upon starting and saving back to database upon stopping.
  - make sure to put rss.xsl in the right place and add correct XSL URL, so the channel can be correctly displayed.(rss/rss.xsl is the default location)

**Did you personally write _all_ the code you are submitting**
(other than code from the course web page)?
- [x] Yes
- [ ] No

**Did you copy any code from the Internet, or from classmates?**
- [ ] Yes
- [x] No

**Did you collaborate with anyone on this assignment?**
- [ ] Yes
- [x] No
