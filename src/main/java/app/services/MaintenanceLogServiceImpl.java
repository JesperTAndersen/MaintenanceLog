package app.services;

import app.dtos.CreateLogRequest;
import app.dtos.MaintenanceLogDTO;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.User;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.persistence.interfaces.IMaintenanceLogDAO;
import app.persistence.interfaces.IReadOnlyDAO;

import java.util.List;

public class MaintenanceLogServiceImpl implements MaintenanceLogService
{
    private final IMaintenanceLogDAO logDao;
    private final IReadOnlyDAO<Asset> assetDao;
    private final IReadOnlyDAO<User> userDao;

    public MaintenanceLogServiceImpl(IMaintenanceLogDAO logDao, IReadOnlyDAO<Asset> assetDao, IReadOnlyDAO<User> userDao)
    {
        this.logDao = logDao;
        this.assetDao = assetDao;
        this.userDao = userDao;
    }

    @Override
    public MaintenanceLogDTO create(Integer assetId, CreateLogRequest request)
    {
        Asset asset = assetDao.get(assetId);
        User performedBy = userDao.get(request.getPerformedByUserId());

        MaintenanceLog log = new MaintenanceLog(
                request.getPerformedDate(),
                request.getStatus(),
                request.getTaskType(),
                request.getComment(),
                asset,
                performedBy
        );

        return new MaintenanceLogDTO(logDao.create(log));
    }

    @Override
    public MaintenanceLogDTO get(Integer id)
    {
        return new MaintenanceLogDTO(logDao.get(id));
    }

    @Override
    public List<MaintenanceLogDTO> getAll()
    {
        return logDao.getAll().stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByAsset(Integer assetId)
    {
        return logDao.getByAsset(assetId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByAssetAndTask(Integer assetId, TaskType taskType)
    {
        return logDao.getByAssetAndTask(assetId, taskType).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatus(LogStatus status)
    {
        return logDao.getByStatus(status).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatusAndAsset(LogStatus status, Integer assetId)
    {
        return logDao.getByStatusAndAsset(status, assetId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByPerformedUser(Integer userId)
    {
        return logDao.getByPerformedUser(userId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getLogsOnActiveAssets(int limit)
    {
        return logDao.getLogsOnActiveAssets(limit).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }
}