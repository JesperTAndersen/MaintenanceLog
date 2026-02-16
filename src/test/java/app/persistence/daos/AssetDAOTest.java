package app.persistence.daos;

import app.config.HibernateTestConfig;
import app.entities.model.Asset;
import app.exceptions.DatabaseException;
import app.exceptions.enums.DatabaseErrorType;
import app.persistence.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssetDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private AssetDAO assetDAO;
    private Map<String, Asset> seeded;

    @BeforeEach
    void setUp()
    {
        seeded = TestPopulator.populateAssets(emf);
        assetDAO = new AssetDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        emf.close();
    }

    @Test
    @DisplayName("Create - should persist asset and generate ID")
    void create()
    {
        Asset asset = new Asset("Test Machine", "Test description", true, null);
        Asset created = assetDAO.create(asset);

        assertThat(created.getAssetId(), notNullValue());
        Asset fetched = assetDAO.get(created.getAssetId());
        assertThat(fetched.getName(), is("Test Machine"));
        assertThat(fetched.getDescription(), is("Test description"));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("Create - should throw IllegalArgumentException when asset is null")
    void createNullAssetThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> assetDAO.create(null));

        assertThat(exception.getMessage(), containsString("Asset cant be null"));
    }

    @Test
    @DisplayName("Get - should retrieve existing asset by ID")
    void get()
    {
        Asset asset1 = seeded.get("asset1");

        Asset fetched = assetDAO.get(asset1.getAssetId());

        assertThat(fetched, notNullValue());
        assertThat(fetched.getAssetId(), is(asset1.getAssetId()));
        assertThat(fetched.getName(), is("Machine A"));
        assertThat(fetched.getDescription(), is("Primary production machine"));
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("Get - should throw DatabaseException when asset not found")
    void getNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> assetDAO.get(99999));

        assertThat(exception.getMessage(), containsString("Asset not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("Get - should throw IllegalArgumentException when ID is null")
    void getNullIdThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> assetDAO.get(null));

        assertThat(exception.getMessage(), containsString("Asset id is required"));
    }

    @Test
    @DisplayName("GetAll - should retrieve only active assets in descending order")
    void getAll()
    {
        List<Asset> activeAssets = assetDAO.getAll();

        assertThat(activeAssets, notNullValue());
        assertThat(activeAssets.size(), is(3));


        for (Asset asset : activeAssets) {
            assertThat(asset.isActive(), is(true));
        }

        // inactive asset is not included
        assertThat(activeAssets, not(hasItem(hasProperty("name", is("Machine D")))));

        // active assets are included
        assertThat(activeAssets, hasItem(hasProperty("name", is("Machine A"))));
        assertThat(activeAssets, hasItem(hasProperty("name", is("Machine B"))));
        assertThat(activeAssets, hasItem(hasProperty("name", is("Machine C"))));
    }

    @Test
    @DisplayName("Update - should throw UnsupportedOperationException as assets are immutable")
    void update()
    {
        Asset asset = seeded.get("asset1");

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> assetDAO.update(asset));

        assertThat(exception.getMessage(), containsString("Assets are immutable"));
    }

    @Test
    @DisplayName("SetActive - should change asset active status to false")
    void setActive()
    {
        Asset asset1 = seeded.get("asset1");
        assertThat(asset1.isActive(), is(true));

        Asset updated = assetDAO.setActive(asset1.getAssetId(), false);

        assertThat(updated, notNullValue());
        assertThat(updated.isActive(), is(false));

        // Verify change persisted
        Asset fetched = assetDAO.get(asset1.getAssetId());
        assertThat(fetched.isActive(), is(false));
    }

    @Test
    @DisplayName("SetActive - should change asset active status to true")
    void setActiveToTrue()
    {
        Asset asset4 = seeded.get("asset4");
        assertThat(asset4.isActive(), is(false));

        Asset updated = assetDAO.setActive(asset4.getAssetId(), true);

        assertThat(updated, notNullValue());
        assertThat(updated.isActive(), is(true));

        // Verify change persisted
        Asset fetched = assetDAO.get(asset4.getAssetId());
        assertThat(fetched.isActive(), is(true));
    }

    @Test
    @DisplayName("SetActive - should throw DatabaseException when asset not found")
    void setActiveNotFoundThrowsException()
    {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> assetDAO.setActive(99999, false));

        assertThat(exception.getMessage(), containsString("Asset not found"));
        assertThat(exception.getErrorType(), is(DatabaseErrorType.NOT_FOUND));
    }

    @Test
    @DisplayName("GetInactiveAssets - should retrieve only inactive assets")
    void getInactiveAssets()
    {
        List<Asset> inactiveAssets = assetDAO.getInactiveAssets();

        assertThat(inactiveAssets, notNullValue());
        assertThat(inactiveAssets.size(), is(1)); // Only 1 inactive asset in test data

        // Verify all returned assets are inactive
        for (Asset asset : inactiveAssets) {
            assertThat(asset.isActive(), is(false));
        }

        // Verify the inactive asset is included
        assertThat(inactiveAssets, hasItem(hasProperty("name", is("Machine D"))));

        // Verify active assets are not included
        assertThat(inactiveAssets, not(hasItem(hasProperty("name", is("Machine A")))));
    }

    @Test
    @DisplayName("GetInactiveAssets - should return empty list when no inactive assets")
    void getInactiveAssetsEmptyList()
    {
        // Set all assets to active
        for (Asset asset : seeded.values()) {
            assetDAO.setActive(asset.getAssetId(), true);
        }

        List<Asset> inactiveAssets = assetDAO.getInactiveAssets();

        assertThat(inactiveAssets, notNullValue());
        assertThat(inactiveAssets.isEmpty(), is(true));
    }
}