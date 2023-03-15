package com.instanceofcake.springbatchasync.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class EmployeeResultRowMapper implements RowMapper {

  @Override
  public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    Employee row = new Employee();
    row.setId(rs.getInt("id"));
    row.setName(rs.getString("name"));
    row.setRollNumber(rs.getString("staff_number"));
    return row;
  }

}
