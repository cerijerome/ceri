package ceri.ci.admin;

public enum Action {
	view, // builds, build, job
	clear, // builds, build, job
	delete, // builds, build, job
	process, // build events
	purge; // builds
}
