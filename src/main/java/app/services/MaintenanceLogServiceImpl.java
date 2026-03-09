package app.services;

import app.dtos.CreateLogRequest;
import app.dtos.MaintenanceLogDTO;
import app.entities.Asset;
import app.entities.MaintenanceLog;
import app.entities.User;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.persistence.interfaces.IDAO;
import app.persistence.interfaces.IMaintenanceLogDAO;

import java.util.List;

public class MaintenanceLogServiceImpl implements MaintenanceLogService
{
    private final IDAO<MaintenanceLog> logDao;
    private final IMaintenanceLogDAO logDaoExpanded;
    private final IDAO<Asset> assetDao;
    private final IDAO<User> userDao;

    public MaintenanceLogServiceImpl(IDAO<MaintenanceLog> logDao, IMaintenanceLogDAO logDaoExpanded, IDAO<Asset> assetDao, IDAO<User> userDao)
    {
        this.logDao = logDao;
        this.logDaoExpanded = logDaoExpanded;
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
        return logDaoExpanded.getByAsset(assetId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByAssetAndTask(Integer assetId, TaskType taskType)
    {
        return logDaoExpanded.getByAssetAndTask(assetId, taskType).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatus(LogStatus status)
    {
        return logDaoExpanded.getByStatus(status).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByStatusAndAsset(LogStatus status, Integer assetId)
    {
        return logDaoExpanded.getByStatusAndAsset(status, assetId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getByPerformedUser(Integer userId)
    {
        return logDaoExpanded.getByPerformedUser(userId).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }

    @Override
    public List<MaintenanceLogDTO> getLogsOnActiveAssets(int limit)
    {
        return logDaoExpanded.getLogsOnActiveAssets(limit).stream()
                .map(MaintenanceLogDTO::new)
                .toList();
    }
}
