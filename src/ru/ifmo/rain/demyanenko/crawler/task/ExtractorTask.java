package ru.ifmo.rain.demyanenko.crawler.task;

import java.io.IOException;
import java.util.List;

import info.kgeorgiy.java.advanced.crawler.Document;

public class ExtractorTask extends WebTask {
	
	private Document document;
	private List<String> extractedLinks;

	public ExtractorTask(Document document, String link, int depth) {
		super(link, depth);
		this.document = document;
	}

	@Override
	public void execute() {
		try {
			extractedLinks = document.extractLinks();
		} catch (IOException e) {
			//e.printStackTrace();
			setRaisedException(e);
		}
		
		publishTaskWasDone();
	}
	
	public List<String> getResult() {
		return extractedLinks;
	}
}
