package com.worth.ifs;

import com.worth.ifs.security.CustomPermissionEvaluator;
import com.worth.ifs.security.CustomPermissionEvaluator.DtoClassToLookupMethod;
import com.worth.ifs.security.CustomPermissionEvaluator.DtoClassToPermissionsToPermissionsMethods;
import com.worth.ifs.security.CustomPermissionEvaluator.ListOfMethods;
import com.worth.ifs.security.CustomPermissionEvaluator.PermissionsToPermissionsMethods;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * A base class for testing services with Spring Security integrated into them.  PermissionRules-annotated beans are
 * made available as mocks so that we can test the effects of calling service methods against the PermissionRule methods
 * that are available.
 * <p>
 * Subclasses of this base class are therefore able to test the security annotations of their various methods by verifying
 * that individual PermissionRule methods are being called (on their owning mocks)
 */
public abstract class BaseMockSecurityTest extends BaseIntegrationTest {

    @Autowired
    protected GenericApplicationContext applicationContext;

    private Map<Class<?>, Object> mockPermissionRulesBeans;
    private Map<Class<?>, Object> mockPermissionEntityLookupStrategies;

    private DtoClassToPermissionsToPermissionsMethods originalRulesMap;
    private DtoClassToLookupMethod originalLookupStrategyMap;

    /**
     * Look up a Mockito mock for a given {@link com.worth.ifs.security.PermissionRules} annotated bean class
     *
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> T getMockPermissionRulesBean(Class<T> clazz) {
        return (T) mockPermissionRulesBeans.get(clazz);
    }


    /**
     * Look up a Mockito mock for a given {@link com.worth.ifs.security.PermissionEntityLookupStrategies} annotated bean class
     *
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> T getMockPermissionEntityLookupStrategiesBean(Class<T> clazz) {
        return (T) mockPermissionEntityLookupStrategies.get(clazz);
    }

    /**
     * Register a temporary bean definition for the class under test (as provided by getServiceClass()), and replace
     * all PermissionRules with mocks that can be looked up with getMockPermissionRulesBean().
     */
    @Before
    public void setup() {

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        // get the custom permission evaluator from the applicationContext and swap its rulesMap for one containing only
        // Mockito mocks
        CustomPermissionEvaluator permissionEvaluator = (CustomPermissionEvaluator) applicationContext.getBean("customPermissionEvaluator");
            originalRulesMap = (DtoClassToPermissionsToPermissionsMethods) getField(permissionEvaluator, "rulesMap");

        Pair<PermissionRulesClassToMock, DtoClassToPermissionsToPermissionsMethods> mocksAndRecorders =
                generateMockedOutRulesMap(originalRulesMap);

        PermissionRulesClassToMock mocks = mocksAndRecorders.getLeft();
        DtoClassToPermissionsToPermissionsMethods recordingProxies = mocksAndRecorders.getRight();

        mockPermissionRulesBeans = mocks;
        setRuleMap(permissionEvaluator, recordingProxies);

        originalLookupStrategyMap = (DtoClassToLookupMethod) getField(permissionEvaluator, "lookupStrategyMap");

        Pair<LookupClassToMock, DtoClassToLookupMethod> mockedOut = generateMockedOutLookupMap(originalLookupStrategyMap);
        mockPermissionEntityLookupStrategies = mockedOut.getLeft();
        setLookupStrategyMap(permissionEvaluator, mockedOut.getRight());

        setLoggedInUser(newUserResource().build());
    }

    /**
     * Replace the original rulesMap and lookup strategy on the custom permission evaluator
     */
    @After
    public void teardown() {
        CustomPermissionEvaluator permissionEvaluator = (CustomPermissionEvaluator) applicationContext.getBean("customPermissionEvaluator");
        setRuleMap(permissionEvaluator, originalRulesMap);
        setLookupStrategyMap(permissionEvaluator, originalLookupStrategyMap);
    }

    private void setLookupStrategyMap(CustomPermissionEvaluator permissionEvaluator, DtoClassToLookupMethod right) {
        setField(permissionEvaluator, "lookupStrategyMap", right);
    }

    private void setRuleMap(CustomPermissionEvaluator permissionEvaluator, DtoClassToPermissionsToPermissionsMethods recordingProxies) {
        setField(permissionEvaluator, "rulesMap", recordingProxies);
    }

    protected Pair<LookupClassToMock, DtoClassToLookupMethod> generateMockedOutLookupMap(DtoClassToLookupMethod originalLookupStrategyMap) {
        LookupClassToMock mockLookupBeans = new LookupClassToMock();
        DtoClassToLookupMethod newMockLookupMap = new DtoClassToLookupMethod();

        for (Entry<Class<?>, Pair<Object, Method>> entry : originalLookupStrategyMap.entrySet()) {
            Class<?> originalDtoClass = entry.getKey();
            Pair<Object, Method> originalLookupBeansAndMethod = entry.getValue();
            Object originalLookupBeans = originalLookupBeansAndMethod.getLeft();
            Method originalLookupMethod = originalLookupBeansAndMethod.getRight();

            if (!mockLookupBeans.containsKey(originalLookupBeans.getClass())) {
                mockLookupBeans.put(originalLookupBeans.getClass(), mock(originalLookupBeans.getClass()));
            }
            Object mockLookupBean = mockLookupBeans.get(originalLookupBeans.getClass());

            String methodName = originalLookupMethod.getName();
            Class<?>[] methodParameters = originalLookupMethod.getParameterTypes();
            final Method mockLookupMethod;

            try {
                mockLookupMethod = mockLookupBean.getClass().getMethod(methodName, methodParameters);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to look up same method on mock", e);
            }
            newMockLookupMap.put(originalDtoClass, Pair.of(mockLookupBean, mockLookupMethod));
        }

        return Pair.of(mockLookupBeans, newMockLookupMap);
    }

    /**
     * Given the rulesMap from the CustomPermissionEvaluator, this method replaces all of the original @PermissionRules-annotated beans
     * with Mockito mocks, and all of the @PermissionRule-annotated methods on those beans with the equivalent methods from the mocks.
     * <p>
     * This method then returns all of the mocks that it has created (so for each original @PermissionRules class, there will be an
     * equivalent mock) and the new rulesMap containing the mock replacements.
     *
     * @param originalRulesMap
     * @return
     */
    protected Pair<PermissionRulesClassToMock, DtoClassToPermissionsToPermissionsMethods> generateMockedOutRulesMap(
            DtoClassToPermissionsToPermissionsMethods originalRulesMap) {

        PermissionRulesClassToMock mockPermissionRulesBeans = new PermissionRulesClassToMock();
        PermissionRulesClassToMock recordingPermissionRulesBeans = new PermissionRulesClassToMock();

        DtoClassToPermissionsToPermissionsMethods newMockRulesMap = new DtoClassToPermissionsToPermissionsMethods();

        for (Entry<Class<?>, PermissionsToPermissionsMethods> entry : originalRulesMap.entrySet()) {

            Class<?> originalDtoClass = entry.getKey();
            PermissionsToPermissionsMethods originalPermissionBeansAndMethodsByPermission = entry.getValue();

            PermissionsToPermissionsMethods newMockPermissionBeansAndMethodsByPermission = new PermissionsToPermissionsMethods();

            for (Entry<String, ListOfMethods> originalPermissionBeansAndMethods : originalPermissionBeansAndMethodsByPermission.entrySet()) {

                String originalPermission = originalPermissionBeansAndMethods.getKey();
                ListOfMethods originalListOfPermissionMethods = originalPermissionBeansAndMethods.getValue();
                ListOfMethods newRecordingListOfPermissionMethods = new ListOfMethods();

                for (Pair<Object, Method> beanAndPermissionMethods : originalListOfPermissionMethods) {

                    Object originalPermissionRulesBean = beanAndPermissionMethods.getKey();

                    if (!mockPermissionRulesBeans.containsKey(originalPermissionRulesBean.getClass())) {
                        Object mock = mock(originalPermissionRulesBean.getClass());
                        Object decoratedMock = createPermissionRuleMock(mock, originalPermissionRulesBean.getClass());
                        mockPermissionRulesBeans.put(originalPermissionRulesBean.getClass(), mock);
                        recordingPermissionRulesBeans.put(originalPermissionRulesBean.getClass(), decoratedMock);
                    }

                    final Object recordingPermissionsRulesBean = recordingPermissionRulesBeans.get(originalPermissionRulesBean.getClass());
                    Method originalPermissionMethod = beanAndPermissionMethods.getRight();
                    String methodName = originalPermissionMethod.getName();
                    Class<?>[] methodParameters = originalPermissionMethod.getParameterTypes();
                    final Method recordingPermissionMethod;

                    try {
                        recordingPermissionMethod = recordingPermissionsRulesBean.getClass().getMethod(methodName, methodParameters);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Unable to look up same method on mock", e);
                    }

                    newRecordingListOfPermissionMethods.add(Pair.of(recordingPermissionsRulesBean, recordingPermissionMethod));
                }

                newMockPermissionBeansAndMethodsByPermission.put(originalPermission, newRecordingListOfPermissionMethods);
            }

            newMockRulesMap.put(originalDtoClass, newMockPermissionBeansAndMethodsByPermission);
        }

        return Pair.of(mockPermissionRulesBeans, newMockRulesMap);
    }

    protected Object createPermissionRuleMock(Object mock, Class<?> mockClass) {
        return mock;
    }

    protected void assertAccessDenied(Runnable serviceCall, Runnable verifications) {
        try {
            serviceCall.run();
            fail("Expected an AccessDeniedException");
        } catch (AccessDeniedException e) {
            verifications.run();
        }
    }

    public static class PermissionRulesClassToMock extends HashMap<Class<?>, Object> {}

    public static class LookupClassToMock extends HashMap<Class<?>, Object> {}

}