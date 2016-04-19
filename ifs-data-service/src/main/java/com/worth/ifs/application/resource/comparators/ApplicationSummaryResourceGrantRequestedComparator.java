package com.worth.ifs.application.resource.comparators;

import java.math.BigDecimal;
import java.util.Comparator;

import com.worth.ifs.application.resource.ApplicationSummaryResource;

/**
 * Camporator, handling nulls, and using id if grant requested is equal.
 */
public class ApplicationSummaryResourceGrantRequestedComparator extends DualFieldComparator<BigDecimal, Long> implements Comparator<ApplicationSummaryResource> {

	@Override
	public int compare(ApplicationSummaryResource o1, ApplicationSummaryResource o2) {
		
		BigDecimal o1Lead = o1.getGrantRequested();
		BigDecimal o2Lead = o2.getGrantRequested();
		
		Long o1Id = o1.getId();
		Long o2Id = o2.getId();
		
		return compare(o1Lead, o2Lead, o1Id, o2Id);
	}
}