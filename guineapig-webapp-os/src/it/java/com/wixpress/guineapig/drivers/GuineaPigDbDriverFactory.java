package com.wixpress.guineapig.drivers;

import com.wixpress.guineapig.util.GuineaPigDBDriver;
import com.wixpress.guineapig.util.MetaDataTable;

import javax.sql.DataSource;

public class GuineaPigDbDriverFactory {
  public static GuineaPigDBDriver instanceFor(DataSource dataSource) {
      return GuineaPigDBDriver.dbDriver(
              new MetaDataTable(dataSource)
      );
  }
}
