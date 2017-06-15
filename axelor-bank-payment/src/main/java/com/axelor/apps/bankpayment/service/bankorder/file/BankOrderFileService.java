/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder.file;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.apps.tool.xml.Marschaller;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;

public class BankOrderFileService {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	protected final String FILE_EXTENSION_XML = "xml";
	protected final String FILE_EXTENSION_TXT = "txt";
	
	protected PaymentMode paymentMode;
	protected BankOrderFileFormat bankOrderFileFormat;
	protected LocalDate bankOrderDate;
	protected BankDetails senderBankDetails;
	protected Company senderCompany;
	protected Currency bankOrderCurrency;
	protected BigDecimal bankOrderTotalAmount;
	protected BigDecimal arithmeticTotal;
	protected int nbOfLines;
	protected LocalDateTime generationDateTime;
	protected String bankOrderSeq;
	protected boolean isMultiDates;
	protected boolean isMultiCurrencies;
	
	protected List<BankOrderLine> bankOrderLineList;
	protected Object fileToCreate;
	protected String context;
	protected String fileExtension;
	
	public BankOrderFileService(BankOrder bankOrder)  {
		
		this.paymentMode = bankOrder.getPaymentMode();
		this.bankOrderFileFormat = bankOrder.getBankOrderFileFormat();
		this.bankOrderDate = bankOrder.getBankOrderDate();
		this.senderBankDetails = bankOrder.getSenderBankDetails();
		this.senderCompany = bankOrder.getSenderCompany();
		this.bankOrderCurrency = bankOrder.getBankOrderCurrency();
		this.bankOrderTotalAmount = bankOrder.getBankOrderTotalAmount();
		this.arithmeticTotal = bankOrder.getArithmeticTotal();
		this.nbOfLines = bankOrder.getNbOfLines();
		this.generationDateTime = bankOrder.getFileGenerationDateTime();
		this.bankOrderSeq = bankOrder.getBankOrderSeq();
		this.bankOrderLineList = bankOrder.getBankOrderLineList();
		this.isMultiDates = bankOrder.getIsMultiDate();
		this.isMultiCurrencies = bankOrder.getIsMultiCurrency();
	}
	
	
	protected String getSenderAddress() throws AxelorException  {
		
		Address senderAddress = this.senderCompany.getAddress();
		
		if(senderAddress == null || Strings.isNullOrEmpty(senderAddress.getFullName()))  {
			
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_FILE_NO_SENDER_ADDRESS), senderCompany.getName()), IException.INCONSISTENCY);
			
		}
		
		return senderAddress.getFullName();
		
	}
	
	
	protected String getFolderPath() throws AxelorException  {
		
		String folderPath = paymentMode.getBankOrderExportFolderPath();
		
		if(Strings.isNullOrEmpty(folderPath))  {
			
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_FILE_NO_FOLDER_PATH), paymentMode.getName()), IException.INCONSISTENCY);
			
		}
		
		return folderPath;
		
	}
	
	
	 /**
	  * Create the order XML file
	 * @throws AxelorException 
	 * @throws IOException 
	 * @throws JAXBException 
	  */
    @SuppressWarnings("unchecked")
	public File generateFile() throws JAXBException, IOException, AxelorException, DatatypeConfigurationException  {
		
		switch (fileExtension) {
		case FILE_EXTENSION_XML:
			
			return Marschaller.marschalFile(fileToCreate, context, this.getFolderPath(), this.computeFileName());
			
		case FILE_EXTENSION_TXT:
			
			try {
				return FileTool.writer(this.getFolderPath(), this.computeFileName(), (List<String>) fileToCreate);
			} catch (IOException e) {
				throw new AxelorException(String.format(I18n.get(com.axelor.apps.account.exception.IExceptionMessage.CFONB_EXPORT_2),
						GeneralServiceImpl.EXCEPTION,e), IException.CONFIGURATION_ERROR);
			}

		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_FILE_UNKNOW_FORMAT), paymentMode.getName()), IException.INCONSISTENCY);
		}
		
		
	}
	
	
	public String computeFileName()  {
		
		return String.format("%s%s.%s", 
				bankOrderFileFormat.getOrderFileFormatSelect(), 
				generationDateTime.toString("yyyy-MM-dd'T'HH:mm:ss"), 
				fileExtension);
	}
	
}
