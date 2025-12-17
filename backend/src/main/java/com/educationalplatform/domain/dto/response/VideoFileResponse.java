package com.educationalplatform.domain.dto.response;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFileResponse {
  private InputStream video;
  private MediaType mediaType;
}
