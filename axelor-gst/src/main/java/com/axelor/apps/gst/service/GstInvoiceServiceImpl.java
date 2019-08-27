package com.axelor.apps.gst.service;

import java.math.BigDecimal;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class GstInvoiceServiceImpl extends InvoiceServiceProjectImpl {

  @Inject
  public GstInvoiceServiceImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService);
  }

  @Override
  public Invoice compute(Invoice invoice) throws AxelorException {

    invoice = super.compute(invoice);
    invoice.setNetCgst(BigDecimal.ZERO);
    invoice.setNetSgst(BigDecimal.ZERO);
    invoice.setNetIgst(BigDecimal.ZERO);
    BigDecimal temp = BigDecimal.ZERO;
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      // Calculating Igst
      invoice.setNetIgst(invoice.getNetIgst().add(invoiceLine.getIgst()));

      // Calculating Sgst
      invoice.setNetSgst(invoice.getNetSgst().add(invoiceLine.getSgst()));

      // Calculating Cgst
      invoice.setNetCgst(invoice.getNetCgst().add(invoiceLine.getCgst()));
      System.err.println(invoiceLine.getIgst()+", "+invoiceLine.getCgst() ); 
      if (invoiceLine.getTaxLine() == null) {
        if (invoiceLine.getGstRate().compareTo(BigDecimal.ZERO) != 0
            && (invoiceLine.getIgst().compareTo(BigDecimal.ZERO) != 0
                || invoiceLine.getCgst().compareTo(BigDecimal.ZERO) != 0)) {
          temp = invoiceLine.getGstRate().multiply(invoiceLine.getExTaxTotal());
          // In the invoice currency
          invoice.setTaxTotal(invoice.getTaxTotal().add(temp));
          invoice.setInTaxTotal(invoice.getInTaxTotal().add(temp));

          // In the company accounting currency
          invoice.setCompanyTaxTotal(invoice.getCompanyTaxTotal().add(temp));
          invoice.setCompanyInTaxTotal(invoice.getCompanyInTaxTotal().add(temp));
        }
      }
    }

    invoice.setAmountRemaining(invoice.getInTaxTotal());

    return invoice;
  }
}
