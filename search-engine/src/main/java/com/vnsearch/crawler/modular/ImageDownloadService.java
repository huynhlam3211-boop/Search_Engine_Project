package com.vnsearch.crawler.modular;

import com.vnsearch.crawler.DnsResolver;
import com.vnsearch.crawler.HtmlDownloader;
import com.vnsearch.crawler.SeedUrlValidator;
import com.vnsearch.crawler.UrlCanonicalizer;
import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.ImageFound;
import com.vnsearch.crawler.bus.PageEvent;
import com.vnsearch.crawler.bus.PageEventHandler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class ImageDownloadService implements PageEventHandler { 
    
}
