/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities;

import com.univocity.*;

/**
 *
 * Just a class from where you can launch the uniVocity license request wizard, if you are interested
 * in seeing how uniVocity performs with batching enabled.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public class RequestLicense {

	/**
	 *  Simply launches the LicenseRequestWizard from the univocity.jar
	 */
	public static void main(String... args) throws Exception {
		LicenseRequestWizard.main(args);
	}
}
