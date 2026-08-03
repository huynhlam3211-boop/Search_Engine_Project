package com.vnsearch.service;

public enum CrawlStatus {

    STARTED {
        @Override
        public boolean canTransitionTo(CrawlStatus next){
            return next == RUNNING || next == FAILED;
        }
    },

    RUNNING {
        @Override
        public boolean canTransitionTo(CrawlStatus next) {
            return next == DONE || next == FAILED;
        }
    },

    DONE {
        @Override
        public boolean canTransitionTo(CrawlStatus next) {
            return false;
        }
    },

    FAILED {
        @Override
        public boolean canTransitionTo(CrawlStatus next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(CrawlStatus next);
    public boolean isTerminal() {
        return this == DONE || this == FAILED;
    }

}
