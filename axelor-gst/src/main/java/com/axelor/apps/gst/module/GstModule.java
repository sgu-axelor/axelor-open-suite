package com.axelor.apps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.service.GstAccountManagementServiceImpl;
import com.axelor.apps.gst.service.GstInvoiceLineServiceImpl;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(InvoiceLineProjectServiceImpl.class).to(GstInvoiceLineServiceImpl.class);
    bind(AccountManagementServiceAccountImpl.class).to(GstAccountManagementServiceImpl.class);
    bind(FiscalPositionService.class).to(FiscalPositionServiceImpl.class);
  }
}
