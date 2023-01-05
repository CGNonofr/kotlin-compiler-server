package com.compiler.server.controllers

import com.compiler.server.model.ErrorDescriptor
import com.compiler.server.model.ExecutionResult
import com.compiler.server.model.Project
import com.compiler.server.model.TranslationJSResult
import com.compiler.server.model.bean.VersionInfo
import com.compiler.server.service.KotlinProjectExecutor
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(value = ["/api/compiler", "/api/**/compiler"])
class CompilerRestController(private val kotlinProjectExecutor: KotlinProjectExecutor) {
  @PostMapping("/compile")
  fun compileKotlinProjectEndpoint(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("application/java-archive")
    kotlinProjectExecutor.compile(request.getInputStream(), response.getOutputStream()){ success ->
      response.setStatus(if (success) 200 else 400)
    }
  }

  @PostMapping("/run")
  fun executeKotlinProjectEndpoint(@RequestBody project: Project): ExecutionResult {
    return kotlinProjectExecutor.run(project)
  }

  @PostMapping("/test")
  fun testKotlinProjectEndpoint(@RequestBody project: Project): ExecutionResult {
    return kotlinProjectExecutor.test(project)
  }

  @PostMapping("/translate")
  fun translateKotlinProjectEndpoint(
    @RequestBody project: Project,
    @RequestParam(defaultValue = "false") ir: Boolean
  ): TranslationJSResult {
    return if (ir) kotlinProjectExecutor.convertToJsIr(project)
    else kotlinProjectExecutor.convertToJs(project)
  }

  @PostMapping("/complete")
  fun getKotlinCompleteEndpoint(
    @RequestBody project: Project,
    @RequestParam line: Int,
    @RequestParam ch: Int
  ) = kotlinProjectExecutor.complete(project, line, ch)

  @PostMapping("/highlight")
  fun highlightEndpoint(@RequestBody project: Project): Map<String, List<ErrorDescriptor>> =
    kotlinProjectExecutor.highlight(project)
}

@RestController
class VersionRestController(private val kotlinProjectExecutor: KotlinProjectExecutor) {
  @GetMapping("/versions")
  fun getKotlinVersionEndpoint(): List<VersionInfo> = listOf(kotlinProjectExecutor.getVersion())
}