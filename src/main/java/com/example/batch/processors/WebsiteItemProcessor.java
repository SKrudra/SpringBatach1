package com.example.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.example.batch.beans.Website;

public class WebsiteItemProcessor implements ItemProcessor<Website, Website> {

	private static final Logger log = LoggerFactory.getLogger(WebsiteItemProcessor.class);

	@Override
	public Website process(final Website website) throws Exception {
		final String url = website.getUrl().toUpperCase();
		final Website transformedWebsite = new Website(website.getId(), url);
		log.info("Converting (" + website + ") into (" + transformedWebsite + ")");
		return transformedWebsite;
	}

}
