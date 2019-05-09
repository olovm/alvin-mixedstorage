package se.uu.ub.cora.alvin.mixedstorage.user;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.connection.ParameterConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReaderImp;

public class RealDbTest {

	private LoggerFactorySpy loggerFactorySpy;
	private UserStorageSpy userStorageForGuest;
	private DataReaderImp dataReaderForUsers;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		userStorageForGuest = new UserStorageSpy();
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://alvin-cora-docker-postgresql:5432/alvin", "alvin", "alvin");
		dataReaderForUsers = DataReaderImp.usingSqlConnectionProvider(sProvider);

	}

	@Test(enabled = false)
	private void test() {
		AlvinMixedUserStorage userStorage = AlvinMixedUserStorage
				.usingUserStorageForGuestAndDataReaderForUsers(userStorageForGuest,
						dataReaderForUsers);
		DataGroup userByIdFromLogin = userStorage.getUserByIdFromLogin("olfel499@user.uu.se");
		assertNotNull(userByIdFromLogin);
	}

	@Test(enabled = false)
	private void testMadde() {
		AlvinMixedUserStorage userStorage = AlvinMixedUserStorage
				.usingUserStorageForGuestAndDataReaderForUsers(userStorageForGuest,
						dataReaderForUsers);
		DataGroup userByIdFromLogin = userStorage.getUserByIdFromLogin("maken168@user.uu.se");
		assertNotNull(userByIdFromLogin);
	}

}
