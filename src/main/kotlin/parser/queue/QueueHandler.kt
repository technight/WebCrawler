package parser.queue

import store.HtmlDTO

interface QueueHandler {
    fun popFromQueue(): HtmlDTO?
}