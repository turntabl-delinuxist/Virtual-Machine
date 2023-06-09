package com.vmorg.requestengine;

import com.vmorg.auth.AuthorisingService;
import com.vmorg.build.SystemBuildService;
import com.vmorg.custom_exception.MachineNotCreatedException;
import com.vmorg.custom_exception.UserNotEntitledException;
import com.vmorg.virtualmachine.Desktop;
import com.vmorg.virtualmachine.Machine;
import com.vmorg.virtualmachine.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestEngineTest {
    @Mock
    AuthorisingService authorisingServiceMock;

    @Mock
    SystemBuildService systemBuildServiceMock;
    Machine windows;
    Machine linux;

    RequestEngine requestEngine;

    @BeforeEach
    void setUp() {
        windows = new Desktop("host2020", "Mike", 1, 4, 200, "Windows 10", "hr498");
        linux = new Server("host2021","Mike",4,8,500,"Ubuntu",8,"s83424h","admin.gh");
        requestEngine = new RequestEngine(authorisingServiceMock, systemBuildServiceMock);
    }

    @Test
    void shouldThrowUserNotEntitledException() {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(false);

        //then
        assertThrows(UserNotEntitledException.class, () -> requestEngine.createNewRequest(windows));
    }

    @Test
    void shouldThrowMachineNotCreatedException() {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("");

        //then
        assertThrows(MachineNotCreatedException.class, () -> requestEngine.createNewRequest(windows));
    }

    @Test
    void shouldIncreaseTotalFailedBuilds() {
        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("");
        try {
            requestEngine.createNewRequest(windows);
        }catch (MachineNotCreatedException | UserNotEntitledException e){
            System.out.println(e.getMessage());
        }

        //then
        assertEquals(1,requestEngine.totalFailedBuildsForDay());
    }

    @Test
    void shouldIncreaseTotalBuildMachines() throws MachineNotCreatedException, UserNotEntitledException {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("Windows");

        requestEngine.createNewRequest(windows);

        //then
        assertEquals(1,requestEngine.totalBuildsByUserForDay().size());
    }


    @Test
    void shouldContainRequestorNameInTotalBuildMachines() throws MachineNotCreatedException, UserNotEntitledException {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("Windows");

        requestEngine.createNewRequest(windows);

        //then
        assertTrue(requestEngine.totalBuildsByUserForDay().containsKey(windows.getRequestorName()));
    }

    @Test
    void shouldContainMachineTypeInTotalBuildMachines() throws MachineNotCreatedException, UserNotEntitledException {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("Windows");

        requestEngine.createNewRequest(windows);
        Map<String,Integer> data = requestEngine.totalBuildsByUserForDay().get(windows.getRequestorName());

        //then
        assertTrue(data.containsKey(windows.toString()));
        assertEquals(1,data.get(windows.toString()));
    }

    @Test
    void shouldContainMachineTypeAndIncreaseQuantity() throws MachineNotCreatedException, UserNotEntitledException {

        //when
        when(authorisingServiceMock.isAuthorised(windows.getRequestorName())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(windows)).thenReturn("Windows");

        requestEngine.createNewRequest(windows);
        requestEngine.createNewRequest(windows);
        Map<String,Integer> data = requestEngine.totalBuildsByUserForDay().get(windows.getRequestorName());

        //then
        assertTrue(data.containsKey(windows.toString()));
        assertEquals(2,data.get(windows.toString()));
    }

    @Test
    void shouldContainTwoDifferentMachines() throws MachineNotCreatedException, UserNotEntitledException {
        //when
        when(authorisingServiceMock.isAuthorised(anyString())).thenReturn(true);
        when(systemBuildServiceMock.createNewMachine(any(Machine.class))).thenReturn("Windows");

        requestEngine.createNewRequest(windows);
        requestEngine.createNewRequest(linux);
        Map<String,Integer> data = requestEngine.totalBuildsByUserForDay().get(windows.getRequestorName());

        //then
        assertEquals(data.size(),2);
        assertTrue(data.containsKey(windows.toString()));
        assertTrue(data.containsKey(linux.toString()));
    }
}