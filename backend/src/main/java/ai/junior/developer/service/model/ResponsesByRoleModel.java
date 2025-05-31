package ai.junior.developer.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ResponsesByRoleModel {
    List<ResponsesItemModel> user;
    List<ResponsesItemModel> assistant;
    List<ResponsesItemModel> functionCall;
}
