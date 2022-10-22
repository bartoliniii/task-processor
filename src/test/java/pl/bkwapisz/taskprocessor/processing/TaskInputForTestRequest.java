package pl.bkwapisz.taskprocessor.processing;

record TaskInputForTestRequest(String input, String pattern) {
    static TaskInputForTestRequest createNotEmpty() {
        return new TaskInputForTestRequest("input", "pattern");
    }
}
