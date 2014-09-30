package edu.hawaii.senin.trajectory.android.persistence;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.LocalDate;

public class LocalDateDateTypeHandler implements TypeHandler<LocalDate> {

  @Override
  public void setParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType)
      throws SQLException {
    if (null != parameter) {
      Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      cal.clear();
      cal.set(parameter.getYear(), parameter.getMonthOfYear() - 1, parameter.getDayOfMonth());
      ps.setDate(i, new Date(cal.getTimeInMillis()));
    }
    else {
      ps.setTimestamp(i, null);
    }
  }

  @Override
  public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
    Date date = rs.getDate(columnName);
    if (null == date) {
      return null;
    }
    return new LocalDate(date.getTime());
  }

  @Override
  public LocalDate getResult(CallableStatement cs, int columnIndex) throws SQLException {
    Date date = cs.getDate(columnIndex);
    return new LocalDate(date.getTime());
  }

  @Override
  public LocalDate getResult(ResultSet rs, int columnIndex) throws SQLException {
    Date date = rs.getDate(columnIndex);
    return new LocalDate(date.getTime());
  }

}
