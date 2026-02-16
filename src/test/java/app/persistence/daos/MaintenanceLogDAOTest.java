package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.entities.model.Asset;
import app.entities.model.MaintenanceLog;
import app.entities.model.User;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MaintenanceLogDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private MaintenanceLogDAO logDAO;
    private Map<String, MaintenanceLog> seededLogs;
    private Map<String, User> seededUsers;
    private Map<String, Asset> seededAssets;

    @BeforeEach
    void setUp()
    {
        seededUsers = TestPopulator.populateUsers(emf);
        seededAssets = TestPopulator.populateAssets(emf);
        seededLogs = TestPopulator.populateMaintenanceLogs(emf, seededUsers, seededAssets);
        logDAO = new MaintenanceLogDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        emf.close();
    }

    @Test
    @DisplayName("Create - should persist log and generate ID")
    void create()
    {
        User user = seededUsers.get("user1");
        Asset asset = seededAssets.get("asset1");
        MaintenanceLog log = new MaintenanceLog(
                LocalDate.now(),
                LogStatus.DONE,
                TaskType.MAINTENANCE,
                "Test maintenance log",
                asset,
                user
        );

        MaintenanceLog created = logDAO.create(log);

        assertThat(created.getLogId(), notNullValue());
        MaintenanceLog fetched = logDAO.get(created.getLogId());
        assertThat(fetched.getComment(), is("Test maintenance log"));
        assertThat(fetched.getStatus(), is(LogStatus.DONE));
        assertThat(fetched.getTaskType(), is(TaskType.MAINTENANCE));
        assertThat(fetched.getAsset().getAssetId(), is(asset.getAssetId()));
        assertThat(fetched.getPerformedBy().getUserId(), is(user.getUserId()));
    }

    @Test
    @DisplayName("Create - should throw IllegalArgumentException when log is null")
    void createNullLogThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.create(null));

        assertThat(exception.getMessage(), containsString("Log cant be null"));
    }

    @Test
    @DisplayName("Get - should retrieve existing log by ID")
    void get()
    {
        MaintenanceLog log1 = seededLogs.get("log1");

        MaintenanceLog fetched = logDAO.get(log1.getLogId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getLogId(), is(log1.getLogId()));
        assertThat(fetched.getComment(), is("Regular maintenance completed"));
        assertThat(fetched.getStatus(), is(LogStatus.DONE));
        assertThat(fetched.getTaskType(), is(TaskType.MAINTENANCE));
    }

    @Test
    @DisplayName("Get - should throw DatabaseException when log not found")
    void getNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> logDAO.get(99999));

        assertThat(exception.getMessage(), containsString("Log not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Get - should throw IllegalArgumentException when ID is null")
    void getNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.get(null));

        assertThat(exception.getMessage(), containsString("Log id is required"));
    }

    @Test
    @DisplayName("GetAll - should retrieve all maintenance logs")
    void getAll()
    {
        List<MaintenanceLog> allLogs = logDAO.getAll();

        assertThat(allLogs, notNullValue());

        // logs with different statuses are included
        long doneCount = allLogs.stream().filter(l -> l.getStatus() == LogStatus.DONE).count();
        long failedCount = allLogs.stream().filter(l -> l.getStatus() == LogStatus.FAILED).count();

        assertThat(doneCount, is(4L));
        assertThat(failedCount, is(2L));
    }

    @Test
    @DisplayName("Update - should throw UnsupportedOperationException as logs are immutable")
    void update()
    {
        MaintenanceLog log = seededLogs.get("log1");

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> logDAO.update(log));

        assertThat(exception.getMessage(), containsString("Maintenance logs are immutable"));
    }

    @Test
    @DisplayName("GetByAsset - should retrieve all logs for specific asset")
    void getByAsset()
    {
        Asset asset1 = seededAssets.get("asset1");

        List<MaintenanceLog> logs = logDAO.getByAsset(asset1.getAssetId());

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(2)); // log1 and log2 are for asset1

        // logs belong to asset1
        for (MaintenanceLog log : logs) {
            assertThat(log.getAsset().getAssetId(), is(asset1.getAssetId()));
        }

        // specific logs are included
        assertThat(logs, hasItem(hasProperty("comment", is("Regular maintenance completed"))));
        assertThat(logs, hasItem(hasProperty("comment", is("Production run successful"))));
    }

    @Test
    @DisplayName("GetByAsset - should return empty list for asset with no logs")
    void getByAssetNoLogs()
    {
        // Create a new asset with no logs
        AssetDAO assetDAO = new AssetDAO(emf);
        Asset newAsset = assetDAO.create(new Asset("New Machine", "No logs yet", true, null));

        List<MaintenanceLog> logs = logDAO.getByAsset(newAsset.getAssetId());

        assertThat(logs, notNullValue());
        assertThat(logs.isEmpty(), is(true));
    }

    @Test
    @DisplayName("GetByAsset - should throw IllegalArgumentException when assetId is null")
    void getByAssetNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByAsset(null));

        assertThat(exception.getMessage(), containsString("Asset id is required"));
    }

    @Test
    @DisplayName("GetByAssetAndTask - should retrieve logs for specific asset and task type")
    void getByAssetAndTask()
    {
        Asset asset1 = seededAssets.get("asset1");

        List<MaintenanceLog> logs = logDAO.getByAssetAndTask(asset1.getAssetId(), TaskType.MAINTENANCE);

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(1)); // log1 is MAINTENANCE for asset1

        MaintenanceLog log = logs.get(0);
        assertThat(log.getAsset().getAssetId(), is(asset1.getAssetId()));
        assertThat(log.getTaskType(), is(TaskType.MAINTENANCE));
        assertThat(log.getComment(), is("Regular maintenance completed"));
    }

    @Test
    @DisplayName("GetByAssetAndTask - should return empty list when no matching logs")
    void getByAssetAndTaskNoMatches()
    {
        Asset asset3 = seededAssets.get("asset3");

        List<MaintenanceLog> logs = logDAO.getByAssetAndTask(asset3.getAssetId(), TaskType.ERROR);

        assertThat(logs, notNullValue());
        assertThat(logs.isEmpty(), is(true));
    }

    @Test
    @DisplayName("GetByAssetAndTask - should throw IllegalArgumentException when assetId is null")
    void getByAssetAndTaskNullAssetIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByAssetAndTask(null, TaskType.MAINTENANCE));

        assertThat(exception.getMessage(), containsString("Asset id is required"));
    }

    @Test
    @DisplayName("GetByAssetAndTask - should throw IllegalArgumentException when taskType is null")
    void getByAssetAndTaskNullTaskTypeThrowsException()
    {
        Asset asset1 = seededAssets.get("asset1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByAssetAndTask(asset1.getAssetId(), null));

        assertThat(exception.getMessage(), containsString("Task type is required"));
    }

    @Test
    @DisplayName("GetByStatus - should retrieve all logs with specific status")
    void getByStatus()
    {
        List<MaintenanceLog> doneLogs = logDAO.getByStatus(LogStatus.DONE);

        assertThat(doneLogs, notNullValue());
        assertThat(doneLogs.size(), is(4)); // 4 DONE logs in test data

        // all logs have DONE status
        for (MaintenanceLog log : doneLogs) {
            assertThat(log.getStatus(), is(LogStatus.DONE));
        }
    }

    @Test
    @DisplayName("GetByStatus - should retrieve failed logs")
    void getByStatusFailed()
    {
        List<MaintenanceLog> failedLogs = logDAO.getByStatus(LogStatus.FAILED);

        assertThat(failedLogs, notNullValue());
        assertThat(failedLogs.size(), is(2)); // 2 FAILED logs in test data

        // all logs have FAILED status
        for (MaintenanceLog log : failedLogs) {
            assertThat(log.getStatus(), is(LogStatus.FAILED));
        }

        // specific failed logs
        assertThat(failedLogs, hasItem(hasProperty("comment", is("Error occurred during operation"))));
        assertThat(failedLogs, hasItem(hasProperty("comment", is("Machine malfunction"))));
    }

    @Test
    @DisplayName("GetByStatus - should throw IllegalArgumentException when status is null")
    void getByStatusNullThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByStatus(null));

        assertThat(exception.getMessage(), containsString("Status is required"));
    }

    @Test
    @DisplayName("GetByStatusAndAsset - should retrieve logs for specific status and asset")
    void getByStatusAndAsset()
    {
        Asset asset2 = seededAssets.get("asset2");

        List<MaintenanceLog> logs = logDAO.getByStatusAndAsset(LogStatus.DONE, asset2.getAssetId());

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(1)); // Only log4 is DONE for asset2

        MaintenanceLog log = logs.get(0);
        assertThat(log.getStatus(), is(LogStatus.DONE));
        assertThat(log.getAsset().getAssetId(), is(asset2.getAssetId()));
        assertThat(log.getComment(), is("Preventive maintenance"));
    }

    @Test
    @DisplayName("GetByStatusAndAsset - should return empty list when no matches")
    void getByStatusAndAssetNoMatches()
    {
        Asset asset3 = seededAssets.get("asset3");

        List<MaintenanceLog> logs = logDAO.getByStatusAndAsset(LogStatus.FAILED, asset3.getAssetId());

        assertThat(logs, notNullValue());
        assertThat(logs.isEmpty(), is(true));
    }

    @Test
    @DisplayName("GetByStatusAndAsset - should throw IllegalArgumentException when status is null")
    void getByStatusAndAssetNullStatusThrowsException()
    {
        Asset asset1 = seededAssets.get("asset1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByStatusAndAsset(null, asset1.getAssetId()));

        assertThat(exception.getMessage(), containsString("Status is required"));
    }

    @Test
    @DisplayName("GetByStatusAndAsset - should throw IllegalArgumentException when assetId is null")
    void getByStatusAndAssetNullAssetIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByStatusAndAsset(LogStatus.DONE, null));

        assertThat(exception.getMessage(), containsString("Asset id is required"));
    }

    @Test
    @DisplayName("GetByPerformedUser - should retrieve all logs performed by specific user")
    void getByPerformedUser()
    {
        User user1 = seededUsers.get("user1");

        List<MaintenanceLog> logs = logDAO.getByPerformedUser(user1.getUserId());

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(4)); // user1 performed 4 logs

        // all logs are performed by user1
        for (MaintenanceLog log : logs) {
            assertThat(log.getPerformedBy().getUserId(), is(user1.getUserId()));
        }
    }

    @Test
    @DisplayName("GetByPerformedUser - should return empty list for user with no logs")
    void getByPerformedUserNoLogs()
    {
        User user3 = seededUsers.get("user3"); // Jeff has no logs

        List<MaintenanceLog> logs = logDAO.getByPerformedUser(user3.getUserId());

        assertThat(logs, notNullValue());
        assertThat(logs.isEmpty(), is(true));
    }

    @Test
    @DisplayName("GetByPerformedUser - should throw IllegalArgumentException when userId is null")
    void getByPerformedUserNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getByPerformedUser(null));

        assertThat(exception.getMessage(), containsString("User id is required"));
    }

    @Test
    @DisplayName("GetLogsOnActiveAssets - should retrieve logs only for active assets with limit")
    void getLogsOnActiveAssets()
    {
        List<MaintenanceLog> logs = logDAO.getLogsOnActiveAssets(10);

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(5)); // 5 logs on active assets (asset4 is inactive)

        //all logs belong to active assets
        for (MaintenanceLog log : logs) {
            assertThat(log.getAsset().isActive(), is(true));
        }

        //log on inactive asset is not included
        assertThat(logs, not(hasItem(hasProperty("comment", is("Machine malfunction")))));
    }

    @Test
    @DisplayName("GetLogsOnActiveAssets - should respect limit parameter")
    void getLogsOnActiveAssetsWithLimit()
    {
        List<MaintenanceLog> logs = logDAO.getLogsOnActiveAssets(3);

        assertThat(logs, notNullValue());
        assertThat(logs.size(), is(3)); // Should return only 3 logs

        //all logs belong to active assets
        for (MaintenanceLog log : logs) {
            assertThat(log.getAsset().isActive(), is(true));
        }
    }

    @Test
    @DisplayName("GetLogsOnActiveAssets - should throw IllegalArgumentException when limit is zero")
    void getLogsOnActiveAssetsZeroLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getLogsOnActiveAssets(0));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }

    @Test
    @DisplayName("GetLogsOnActiveAssets - should throw IllegalArgumentException when limit is negative")
    void getLogsOnActiveAssetsNegativeLimitThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logDAO.getLogsOnActiveAssets(-1));

        assertThat(exception.getMessage(), containsString("Input needs to be bigger than 0"));
    }
}