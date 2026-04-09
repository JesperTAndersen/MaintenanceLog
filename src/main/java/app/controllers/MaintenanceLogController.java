package app.controllers;

import app.dtos.CreateLogRequest;
import app.entities.enums.LogStatus;
import app.entities.enums.TaskType;
import app.exceptions.ApiException;
import app.services.interfaces.EmployeeIdentityService;
import app.services.interfaces.MaintenanceLogService;
import app.utils.EmployeeAuthUtil;
import io.javalin.http.Context;

public class MaintenanceLogController
{
    private final MaintenanceLogService logService;
    private final EmployeeIdentityService employeeIdentityService;

    public MaintenanceLogController(MaintenanceLogService logService, EmployeeIdentityService employeeIdentityService)
    {
        this.logService = logService;
        this.employeeIdentityService = employeeIdentityService;
    }

    public void createLogForAsset(Context ctx)
    {
        int assetId = Integer.parseInt(ctx.pathParam("id"));

        Integer performedById = EmployeeAuthUtil.requireAuthenticatedEmployee(ctx, employeeIdentityService).id();

        CreateLogRequest body = ctx.bodyValidator(CreateLogRequest.class)
                .check(dto -> dto.performedDate() != null, "Performed date is required")
                .check(dto -> dto.status() != null, "Status is required")
                .check(dto -> dto.taskType() != null, "Task type is required")
                .check(dto -> dto.comment() != null, "Comment is required")
                .get();

        CreateLogRequest request = new CreateLogRequest(
                body.performedDate(),
                body.status(),
                body.taskType(),
                body.comment(),
                performedById
        );

        ctx.status(201).json(logService.create(assetId, request));
    }

    public void getAll(Context ctx)
    {
        String statusParam = ctx.queryParam("status");

        if (statusParam != null)
        {
            try
            {
                LogStatus status = LogStatus.valueOf(statusParam.toUpperCase());
                ctx.status(200).json(logService.getByStatus(status));
            }
            catch (IllegalArgumentException e)
            {
                throw new ApiException(400, "Invalid status value");
            }
        }
        else
        {
            ctx.status(200).json(logService.getAll());
        }
    }

    public void get(Context ctx)
    {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.status(200).json(logService.get(id));
    }

    public void getByEmployee(Context ctx)
    {
        int employeeId = Integer.parseInt(ctx.pathParam("employeeId"));
        ctx.status(200).json(logService.getByPerformedEmployee(employeeId));
    }

    public void getLogsByAsset(Context ctx)
    {
        int assetId = Integer.parseInt(ctx.pathParam("id"));
        String taskParam = ctx.queryParam("taskType");
        String statusParam = ctx.queryParam("status");

        if (taskParam != null)
        {
            try
            {
                TaskType taskType = TaskType.valueOf(taskParam.toUpperCase());
                ctx.status(200).json(logService.getByAssetAndTask(assetId, taskType));
            }
            catch (IllegalArgumentException e)
            {
                throw new ApiException(400, "Invalid task type value");
            }
        }
        else if (statusParam != null)
        {
            try
            {
                LogStatus status = LogStatus.valueOf(statusParam.toUpperCase());
                ctx.status(200).json(logService.getByStatus(status));
            }
            catch (IllegalArgumentException e)
            {
                throw new ApiException(400, "Invalid status value");
            }
        }
        else
        {
            ctx.status(200).json(logService.getByAsset(assetId));
        }
    }
}