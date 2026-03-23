package app.services.interfaces;

import app.dtos.EmployeeDTO;

import java.util.List;

public interface EmployeeService
{
    EmployeeDTO get(Integer id);

    List<EmployeeDTO> getAll(Boolean active);

    EmployeeDTO update(Integer id, EmployeeDTO employeeDTO);

    EmployeeDTO deactivate(Integer id);

    EmployeeDTO activate(Integer id);
}
