package app.services;

import app.dtos.CreateLogRequest;
import app.dtos.MaintenanceLogDTO;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.Employee;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.mappers.MaintenanceLogMapper;
import app.persistence.interfaces.IMaintenanceLogDAO;
import app.persistence.interfaces.IReadDAO;

import java.util.List;

public class MaintenanceLogServiceImpl implements MaintenanceLogService
{
    private final IMaintenanceLogDAO logDao;
    private final IReadDAO<Asset> assetDao;
    private final IReadDAO<Employee> employeeDao;

    public MaintenanceLogServiceImpl(IMaintenanceLogDAO logDao, IReadDAO<Asset> assetDao, IReadDAO<Employee> employeeDao)
    {
        this.logDao = logDao;
        this.assetDao = assetDao;
        this.employeeDao = employeeDao;
    }

    @Override
    public MaintenanceLogDTO create(Integer assetId, CreateLogRequest request)
    {
        Asset asset = assetDao.get(assetId);
        Employee performedBy = employeeDao.get(request.performedByUserId());

        MaintenanceLog log = new MaintenanceLog(
                request.performedDate(),
                request.status(),
                request.taskType(),
                request.comment(),
                asset,
                performedBy
        );

        return MaintenanceLogMapper.toDTO(logDao.create(log));
    }

    @Override
    public MaintenanceLogDTO get(Integer id)
    {
        return MaintenanceLogMapper.toDTO(logDao.get(id));
    }

    @Override
    public List<MaintenanceLogDTO> getAll()
    {
        return logDao.getAll().stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByAsset(Integer assetId)
    {
        return logDao.getByAsset(assetId).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByAssetAndTask(Integer assetId, TaskType taskType)
    {
        return logDao.getByAssetAndTask(assetId, taskType).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatus(LogStatus status)
    {
        return logDao.getByStatus(status).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatusAndAsset(LogStatus status, Integer assetId)
    {
        return logDao.getByStatusAndAsset(status, assetId).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByPerformedEmployee(Integer userId)
    {
        return logDao.getByPerformedUser(userId).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getLogsOnActiveAssets(int limit)
    {
        return logDao.getLogsOnActiveAssets(limit).stream()
                .map(MaintenanceLogMapper::toDTO)
                .toList();
    }
}