package ru.ifmo.rain.demyanenko.crawler.task;

import java.io.IOException;
import java.net.MalformedURLException;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

public class DownloaderTask extends WebTask{
	private Document doucment;
	private Downloader downloader;
	

	public DownloaderTask(String link, Downloader downloader, int depth) {
		super(link, depth);
		this.downloader = downloader;
	}

	@Override
	public void execute() {
		try {
			doucment = downloader.download(getLink());
		} catch (IOException e) {
			e.printStackTrace();
			setRaisedException(e);
		}
		
		publishTaskWasDone();
	}

	
	public Document getResult() {
		return doucment;
	}
	
	public String getHostName() {
		try {
			return URLUtils.getHost(getLink());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
