import io.grpc.ManagedChannelBuilder
import io.grpc.Status.Code.INVALID_ARGUMENT
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*
import kotlinx.coroutines.*

class IdServerTest {
    companion object {
        @JvmStatic val svr = IdentityServer(50051, false)
        @JvmStatic @BeforeAll fun startServer() {
            Thread{svr.start()}.start()
        }
        @JvmStatic @AfterAll fun stopServer() {
            svr.stop()
        }
    }

    val client = IdentityServiceGrpc.newBlockingStub(
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
    )

    @Test fun createNoError() {
        val createResp = client.createUser(createUserRequest{ loginName = "HomeBoy"; realName = "Homer Simpson"; password = "Doh" })
        val entryResp = client.reverseLookup(reverseLookupUserRequest{ userId = createResp.userId })
        assertEquals("HomeBoy", entryResp.loginName)
        assertEquals("Homer Simpson", entryResp.realName)
        client.deleteUser(deleteUserRequest{loginName = "HomeBoy"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun createDuplicateLogin() {
        client.createUser(createUserRequest{ loginName = "TestDupeCreate"; realName = "Homer Simpson"; password = "Doh" })
        val resp = client.createUser(createUserRequest{ loginName = "TestDupeCreate"; realName = "Homelander"; password = "Pow" })
        assertEquals(false, resp.success)
        client.deleteUser(deleteUserRequest{loginName = "TestDupeCreate"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun modifyNoError() {
        client.createUser(createUserRequest{ loginName = "OldLogin"; realName = "Homer Simpson"; password = "Doh" })
        val modResp = client.modifyUser(modifyUserRequest{oldLoginName = "OldLogin"; newLoginName = "ModifiedLogin"; password = "Doh"})
        assertEquals(true, modResp.success)
        val lookupResp = client.lookup(lookupUserRequest{ loginName = "ModifiedLogin" })
        assertEquals(lookupResp.realName, "Homer Simpson")
        client.deleteUser(deleteUserRequest{loginName = "ModifiedLogin"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun modifyEntryDoesNotExist() {
        val modResp = client.modifyUser(modifyUserRequest{oldLoginName = "Garbage"; newLoginName = "HotGarbage"; password = "Bonk"})
        assertEquals(false, modResp.success)
        assertEquals("Could not find entry matching: Garbage", modResp.message)
        svr.destroyUserCounter()
    }

    @Test fun modifyDuplicateNewLoginName() {
        client.createUser(createUserRequest{ loginName = "MatchingLogin"; realName = "Homer Simpson"; password = "Doh" })
        client.createUser(createUserRequest{ loginName = "Jim"; realName = "Jimmy Neutron"; password = "Weezer" })
        val modResp = client.modifyUser(modifyUserRequest{oldLoginName = "Jim"; newLoginName = "MatchingLogin"; password = "Weezer"})
        assertEquals(false, modResp.success)
        assertEquals("Username MatchingLogin already exists.", modResp.message)
        client.deleteUser(deleteUserRequest{loginName = "MatchingLogin"; password = "Doh"})
        client.deleteUser(deleteUserRequest{loginName = "Jim"; password = "Weezer"})
        svr.destroyUserCounter()
    }

    @Test fun modifyWrongPassword() {
        client.createUser(createUserRequest{ loginName = "ModifyPassword"; realName = "Homer Simpson"; password = "Doh" })
        val modResp = client.modifyUser(modifyUserRequest{oldLoginName = "ModifyPassword"; newLoginName = "Failed:("; password = "Password"})
        assertEquals(false, modResp.success)
        assertEquals("Incorrect password for: ModifyPassword.", modResp.message)
        client.deleteUser(deleteUserRequest{loginName = "ModifyPassword"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun deleteNoError() {
        client.createUser(createUserRequest{ loginName = "EntryToDelete"; realName = "Homer Simpson"; password = "Doh" })
        val delResp = client.deleteUser(deleteUserRequest{loginName = "EntryToDelete"; password = "Doh"})
        val lookResp = client.lookup(lookupUserRequest{ loginName = "EntryToDelete" })
        assertEquals(true, delResp.success)
        assertEquals(false, lookResp.success)
        assertEquals("Could not find entry matching login name.", lookResp.message)
        svr.destroyUserCounter()
    }

    @Test fun deleteEntryDoesNotExist() {
        val delResp = client.deleteUser(deleteUserRequest{loginName = "RuhRohRaggy"; password = "Scoob"})
        assertEquals(false, delResp.success)
        assertEquals("User does not exist.", delResp.message)
        svr.destroyUserCounter()
    }

    @Test fun deleteWrongPassword() {
        client.createUser(createUserRequest{ loginName = "EntryToDelete"; realName = "Homer Simpson"; password = "Doh" })
        val delResp = client.deleteUser(deleteUserRequest{loginName = "EntryToDelete"; password = "OhDoh"})
        val lookResp = client.lookup(lookupUserRequest{ loginName = "EntryToDelete" })
        assertEquals(false, delResp.success)
        assertEquals("Password does not match.", delResp.message)
        assertEquals(true, lookResp.success)
        assertEquals("Homer Simpson", lookResp.realName)
        client.deleteUser(deleteUserRequest{loginName = "EntryToDelete"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun reverselookupNoError() {
        val createResp = client.createUser(createUserRequest{ loginName = "HomeBoy"; realName = "Homer Simpson"; password = "Doh" })
        val entryResp = client.reverseLookup(reverseLookupUserRequest{ userId = createResp.userId })
        assertEquals("HomeBoy", entryResp.loginName)
        assertEquals("Homer Simpson", entryResp.realName)
        client.deleteUser(deleteUserRequest{loginName = "HomeBoy"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun reverseLookupIdNotFound() {
        val resp = client.reverseLookup(reverseLookupUserRequest{ "319489" })
        assertEquals(false, resp.success)
        assertEquals("Could not find user with ID.", resp.message)
        svr.destroyUserCounter()
    }

    @Test fun lookupNoError() {
        val createResp = client.createUser(createUserRequest{ loginName = "HomeBoy"; realName = "Homer Simpson"; password = "Doh" })
        val entryResp = client.lookup(lookupUserRequest{ loginName = "HomeBoy" })
        assertEquals(createResp.userId, entryResp.userId)
        assertEquals("Homer Simpson", entryResp.realName)
        client.deleteUser(deleteUserRequest{loginName = "HomeBoy"; password = "Doh"})
        svr.destroyUserCounter()
    }

    @Test fun lookupLoginNotFound() {
        val resp = client.lookup(lookupUserRequest{ loginName = "Garbage" })
        assertEquals(false, resp.success)
        assertEquals("Could not find entry matching login name.", resp.message)
    }

    @Test fun listLogins() {
        for(i in 1..3) {
            client.createUser(createUserRequest{ loginName = "login$i"; realName = "Bleh$i"; password = "Doh" })
        }
        val resp = client.listLogins(empty{})
        for(i in 1 .. 3) {
            assertTrue(resp.loginNamesList.contains("login$i"))
            client.deleteUser(deleteUserRequest{loginName = "login$i"; password = "Doh"})
        }
        svr.destroyUserCounter()
    }

    @Test fun listIds() {
        val ids = Array(3){""}
        for(i in 1..3) {
            val createResp = client.createUser(createUserRequest{ loginName = "login$i"; realName = "Bleh$i"; password = "Doh" })
            ids.set(i-1, createResp.userId)
        }
        val resp = client.listIds(empty{})
        for(i in 1 .. 3) {
            assertTrue(resp.userIdsList.contains("${ids.get(i-1)}"))
            client.deleteUser(deleteUserRequest{loginName = "login$i"; password = "Doh"})
        }
        svr.destroyUserCounter()
    }

    @Test fun listAllInfo() {
        val entries = mutableListOf<UserEntry>()
        for(i in 1..3) {
            val createResp = client.createUser(createUserRequest{ loginName = "login$i"; realName = "Bleh$i"; password = "Doh" })
            entries.add(userEntry{ userId = createResp.userId; loginName = "login$i"; realName = "Bleh$i" })
        }
        val resp = client.listAllInfo(empty{})
        assertEquals(entries, resp.userEntriesList)
        for(i in 1 .. 3) {
            client.deleteUser(deleteUserRequest{loginName = "login$i"; password = "Doh"})
        }
        svr.destroyUserCounter()
    }

    @Test fun bajillionModifies(){
        client.createUser(createUserRequest{ loginName = "OldLogin"; realName = "Homer Simpson"; password = "Doh" })
        runBlocking {
        for(i in 1..5){
            launch{ client.modifyUser(modifyUserRequest{oldLoginName = "OldLogin"; newLoginName = "ModifiedLogin"; password = "Doh"})} 
        }
        }
        client.deleteUser(deleteUserRequest{loginName = "ModifiedLogin"; password = "Doh"})
        svr.destroyUserCounter()
    }

}
