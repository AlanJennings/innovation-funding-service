package com.worth.ifs.transactional;

import com.worth.ifs.util.Either;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.worth.ifs.assessment.transactional.AssessorServiceImpl.ServiceFailures.UNEXPECTED_ERROR;
import static com.worth.ifs.util.Either.left;
import static com.worth.ifs.util.Either.right;

/**
 * Represents the result of an action, that will be either a failure or a success.  A failure will result in a ServiceFailure, and a
 * success will result in a T.  Additionally, these can be mapped to produce new ServiceResults that either fail or succeed.
 */
public class ServiceResult<T> {

    private static final Log LOG = LogFactory.getLog(ServiceResult.class);

    private Either<ServiceFailure, T> result;

    private ServiceResult(T success) {
        this(right(success));
    }

    private ServiceResult(ServiceFailure failure) {
        this(left(failure));
    }

    private ServiceResult(Either<ServiceFailure, T> result) {
        this.result = result;
    }

    public <T1> T1 mapLeftOrRight(Function<? super ServiceFailure, ? extends T1> lFunc, Function<? super T, ? extends T1> rFunc) {
        return result.mapLeftOrRight(lFunc, rFunc);
    }

    public <R> ServiceResult<R> map(Function<? super T, ServiceResult<R>> rFunc) {

        if (result.isLeft()) {
            return failure(result.getLeft());
        }

        return rFunc.apply(result.getRight());
    }

    public boolean isLeft() {
        return result.isLeft();
    }

    public boolean isRight() {
        return result.isRight();
    }

    public ServiceFailure getLeft() {
        return result.getLeft();
    }

    public T getRight() {
        return result.getRight();
    }

    public static <T1> T1 getLeftOrRight(Either<T1, T1> either) {
        return Either.getLeftOrRight(either);
    }

    public static <T> ServiceResult<T> success(T successfulResult) {
        return new ServiceResult<>(successfulResult);
    }

    public static <T> Supplier<ServiceResult<T>> successSupplier(T successfulResult) {
        return () -> success(successfulResult);
    }

    public static <T> ServiceResult<T> failure(ServiceFailure failure) {
        return new ServiceResult<>(failure);
    }

    public static <T> ServiceResult<T> failure(Enum<?> failureKey) {
        return new ServiceResult<>(ServiceFailure.error(failureKey));
    }

    public static <T> ServiceResult<T> failure(String failureMessage) {
        return new ServiceResult<>(ServiceFailure.error(failureMessage));
    }

    public static <T> Supplier<ServiceResult<T>> failureSupplier(Enum<?> failureKey) {
        return () -> failure(failureKey);
    }

    public static <T> ServiceResult<T> nonNull(T value, Enum<?> failureKey) {

        if (value == null) {
            return failure(failureKey);
        }

        return success(value);
    }

    public static <T> ServiceResult<T> fromEither(Either<ServiceFailure, T> value) {
        return new ServiceResult<>(value);
    }

    public static <T, R> ServiceResult<T> anyFailures(List<ServiceResult<R>> results, Supplier<ServiceResult<T>> failureSupplier, Supplier<ServiceResult<T>> successSupplier) {
        return results.stream().anyMatch(ServiceResult::isLeft) ? failureSupplier.get() : successSupplier.get();
    }

    /**
     * This wrapper wraps the serviceCode function and rolls back transactions upon receiving a ServiceFailure
     * response (an Either with a left of ServiceFailure).
     *
     * It will also catch all exceptions thrown from within serviceCode and convert them into ServiceFailures of
     * type UNEXPECTED_ERROR.
     *
     * @param serviceCode
     * @param <T>
     * @return
     */
    public static <T> ServiceResult<T> handlingErrors(Supplier<ServiceResult<T>> serviceCode) {
        return handlingErrors(UNEXPECTED_ERROR, serviceCode);
    }

    /**
     * This wrapper wraps the serviceCode function and rolls back transactions upon receiving a ServiceFailure
     * response (an Either with a left of ServiceFailure).
     *
     * It will also catch all exceptions thrown from within serviceCode and convert them into ServiceFailures of
     * type UNEXPECTED_ERROR.
     *
     * @param <T>
     * @param serviceCode
     * @return
     */
    public static <T> ServiceResult<T> handlingErrors(Enum<?> catchAllError, Supplier<ServiceResult<T>> serviceCode) {
        try {
            ServiceResult<T> response = serviceCode.get();

            if (response.isLeft()) {
                LOG.debug("Service failure encountered - performing transaction rollback");
                rollbackTransaction();
            }
            return response;
        } catch (Exception e) {
            LOG.warn("Uncaught exception encountered while performing service call.  Performing transaction rollback and returning ServiceFailure", e);
            rollbackTransaction();
            return failure(catchAllError);
        }
    }

    private static void rollbackTransaction() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (NoTransactionException e) {
            LOG.trace("No transaction to roll back");
            LOG.error(e);
        }
    }


}