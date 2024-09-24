package com.easysoftwareinput.application.crawldown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.domain.crawldown.model.RpmCrawlEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class CrawlPkgService {
    /**
     * crawl.
     * @param entity entity.
     * @return list of urls.
     */
    public List<String> crawl(RpmCrawlEntity entity) {
        List<String> downloadUrls = new ArrayList<>();
        String url = entity.getUrl();
        downloadUrls.addAll(recursion(url, entity));

        return downloadUrls;
    }

    /**
     * check url with target.
     * @param url url.
     * @param entity entity.
     * @return boolean.
     */
    public boolean checkTargets(String url, RpmCrawlEntity entity) {
        return url.endsWith(entity.getTarget());
    }

    /**
     * get sub urls from url.
     * @param url url.
     * @param entity entity.
     * @return list of urls.
     */
    public List<String> recursion(String url, RpmCrawlEntity entity) {
        if (!validUrl(url)) {
            return Collections.emptyList();
        }

        List<String> res = new ArrayList<>();
        List<String> subs = getSubMenus(url);
        for (String sub : subs) {
            String newUrl = url + sub;
            if (checkTargets(newUrl, entity)) {
                res.add(newUrl);
                break;
            }

            res.addAll(recursion(newUrl, entity));
        }
        return res;
    }

    /**
     * valid url.
     * @param url url.
     * @return boolean.
     */
    public boolean validUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        if (!url.endsWith("/")) {
            return false;
        }

        if (url.endsWith("../")) {
            return false;
        }

        int httpsCount = StringUtils.countMatches(url, "https://");
        int httpCount = StringUtils.countMatches(url, "http://");
        if (httpsCount + httpCount > 1) {
            return false;
        }

        if (url.contains("Packages")) {
            return false;
        }

        if (url.contains("repodata.old")) {
            return false;
        }

        return true;
    }

    /**
     * get sub menus.
     * @param url url.
     * @return list of sub urls.
     */
    public List<String> getSubMenus(String url) {
        Connection con = Jsoup.connect(url);

        Document doc;
        try {
            doc = con.get();
        } catch (IOException e) {
            return Collections.emptyList();
        }
        Elements elements = doc.select("a[href]");
        return elements.stream().map(e -> e.attr("href")).collect(Collectors.toList());
    }
}
