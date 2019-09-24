module se.uu.ub.cora.alvin.mixedstorage {
	requires transitive se.uu.ub.cora.sqldatabase;
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive java.xml;
	requires transitive se.uu.ub.cora.gatekeeper;
	requires se.uu.ub.cora.logger;
	requires se.uu.ub.cora.basicstorage;
	requires transitive se.uu.ub.cora.storage;
	requires se.uu.ub.cora.searchstorage;
	// requires com.rabbitmq.client;
	// requires qpid.jms.client;
	// requires javax.jms;
	requires javax.jms.api;
	// requires com.rabbitmq.jms;
	// requires com.rabbitmq.client;
	requires activemq.client;
	requires com.rabbitmq.client;
	// requires fedora.client.messaging;
	// requires fedora.client.messaging;
	// requires org
	// requires geronimo.jms.2.0.spec;

	exports se.uu.ub.cora.alvin.mixedstorage.db;
	exports se.uu.ub.cora.alvin.mixedstorage.fedora;
	exports se.uu.ub.cora.alvin.mixedstorage.id;
	exports se.uu.ub.cora.alvin.mixedstorage.parse;
	exports se.uu.ub.cora.alvin.mixedstorage.user;

	provides se.uu.ub.cora.gatekeeper.user.UserStorageProvider
			with se.uu.ub.cora.alvin.mixedstorage.user.FromAlvinClassicUserStorageProvider;
	provides se.uu.ub.cora.storage.RecordIdGeneratorProvider
			with se.uu.ub.cora.alvin.mixedstorage.id.AlvinIdGeneratorProvider;
	provides se.uu.ub.cora.storage.RecordStorageProvider
			with se.uu.ub.cora.alvin.mixedstorage.AlvinMixedRecordStorageProvider;
	provides se.uu.ub.cora.storage.MetadataStorageProvider
			with se.uu.ub.cora.alvin.mixedstorage.AlvinMixedRecordStorageProvider;

	opens place;

}